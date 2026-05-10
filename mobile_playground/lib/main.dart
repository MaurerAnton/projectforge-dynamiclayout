// DynamicLayout mobile playground — test PFDL JSON on iOS and Android.
// Flutter app. Uses the dynamiclayout_flutter.dart renderer.
//
// Features:
//   - 4 preset layouts (About, Feedback, Registration, All Components)
//   - JSON editor with live preview
//   - Error display
//   - Dark/light theme toggle
//
// Build: flutter run

import 'dart:convert';
import 'package:flutter/material.dart';
import '../../dynamiclayout_flutter.dart';
import 'presets.dart';

void main() => runApp(const PlaygroundApp());

class PlaygroundApp extends StatelessWidget {
  const PlaygroundApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DynamicLayout Playground',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorSchemeSeed: Colors.blue,
        useMaterial3: true,
        brightness: Brightness.light,
      ),
      darkTheme: ThemeData(
        colorSchemeSeed: Colors.blue,
        useMaterial3: true,
        brightness: Brightness.dark,
      ),
      home: const PlaygroundScreen(),
    );
  }
}

class PlaygroundScreen extends StatefulWidget {
  const PlaygroundScreen({super.key});

  @override
  State<PlaygroundScreen> createState() => _PlaygroundScreenState();
}

class _PlaygroundScreenState extends State<PlaygroundScreen> {
  int _tab = 0; // 0=Preview, 1=Editor, 2=Presets
  String _preset = 'about';
  Map<String, dynamic> _spec = {};
  Map<String, dynamic> _data = {};
  String _jsonError = '';
  final _jsonCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadPreset(_preset);
  }

  void _loadPreset(String name) {
    final json = presets[name];
    if (json == null) return;
    try {
      final parsed = jsonDecode(json) as Map<String, dynamic>;
      setState(() {
        _spec = parsed['ui'] as Map<String, dynamic>? ?? parsed;
        _data = {};
        _jsonError = '';
        _jsonCtrl.text = const JsonEncoder.withIndent('  ').convert(parsed);
      });
    } catch (e) {
      setState(() => _jsonError = 'Parse error: $e');
    }
  }

  void _applyJson() {
    try {
      final parsed = jsonDecode(_jsonCtrl.text) as Map<String, dynamic>;
      setState(() {
        _spec = parsed['ui'] as Map<String, dynamic>? ?? parsed;
        _data = {};
        _jsonError = '';
      });
    } catch (e) {
      setState(() => _jsonError = 'JSON error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_spec['title']?.toString() ?? 'DynamicLayout Playground'),
        actions: [
          IconButton(icon: const Icon(Icons.light_mode), tooltip: 'Toggle theme',
            onPressed: () {
              final brightness = Theme.of(context).brightness;
              final newTheme = brightness == Brightness.light ? ThemeMode.dark : ThemeMode.light;
              (context.findAncestorWidgetOfExactType<MaterialApp>())?.themeMode == newTheme;
            }),
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert),
            onSelected: (v) {
              if (v == 'reset') _loadPreset(_preset);
              if (v == 'docs') {}
            },
            itemBuilder: (_) => [
              const PopupMenuItem(value: 'reset', child: Text('Reset to preset')),
              const PopupMenuItem(value: 'docs', child: Text('Open docs')),
            ],
          ),
        ],
        bottom: TabBar(
          tabs: const [
            Tab(icon: Icon(Icons.play_arrow), text: 'Preview'),
            Tab(icon: Icon(Icons.code), text: 'JSON'),
            Tab(icon: Icon(Icons.style), text: 'Presets'),
          ],
          onTap: (i) => setState(() => _tab = i),
        ),
      ),
      body: IndexedStack(
        index: _tab,
        children: [
          // Tab 0: Preview
          _spec.isEmpty
              ? const Center(child: Text('Select a preset or paste JSON'))
              : DynamicLayout(
                  spec: _spec,
                  data: _data,
                  onUpdate: (d) => setState(() => _data = d),
                  onAction: (id, action) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Action: $id'), duration: const Duration(seconds: 1)),
                    );
                  },
                ),

          // Tab 1: JSON editor
          Column(children: [
            Expanded(
              child: TextField(
                controller: _jsonCtrl,
                maxLines: null,
                expands: true,
                textAlignVertical: TextAlignVertical.top,
                style: const TextStyle(fontFamily: 'monospace', fontSize: 13),
                decoration: const InputDecoration(
                  border: InputBorder.none,
                  contentPadding: EdgeInsets.all(12),
                  hintText: 'Paste DynamicLayout JSON here...',
                ),
                onChanged: (_) => _applyJson(),
              ),
            ),
            if (_jsonError.isNotEmpty)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(8),
                color: Colors.red.shade50,
                child: Text(_jsonError, style: TextStyle(color: Colors.red.shade800, fontFamily: 'monospace', fontSize: 12)),
              ),
          ]),

          // Tab 2: Presets
          ListView(
            children: [
              _presetCard('about', 'About Page', 'Static info page with labels'),
              _presetCard('feedback', 'Feedback Form', 'Form with name, email, message'),
              _presetCard('registration', 'Registration', 'Two‑column registration form'),
              _presetCard('all', 'All Components', 'Every DynamicLayout component'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _presetCard(String id, String title, String desc) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: ListTile(
        leading: Icon(_preset == id ? Icons.check_circle : Icons.circle_outlined,
            color: _preset == id ? Theme.of(context).colorScheme.primary : null),
        title: Text(title),
        subtitle: Text(desc),
        onTap: () {
          setState(() => _preset = id; _tab = 0);
          _loadPreset(id);
        },
      ),
    );
  }
}
