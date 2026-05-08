import React from 'react';
import DynamicGroup from './components/DynamicGroup';
import DynamicFieldset from './components/DynamicFieldset';
import DynamicInlineGroup from './components/DynamicInlineGroup';
import DynamicLabel from './components/DynamicLabel';
import DynamicButton from './components/DynamicButton';
import DynamicAlert from './components/DynamicAlert';
import DynamicSpacer from './components/DynamicSpacer';
import DynamicBadge from './components/DynamicBadge';
import DynamicBadgeList from './components/DynamicBadgeList';
import DynamicList from './components/DynamicList';
import DynamicInputResolver from './components/input/DynamicInputResolver';
import DynamicCheckbox from './components/input/DynamicCheckbox';
import DynamicTextArea from './components/input/DynamicTextArea';
import DynamicRadioButton from './components/input/DynamicRadioButton';
import DynamicReadonlyField from './components/input/DynamicReadonlyField';

const components = {};

export const registerComponent = (type, tag) => {
    components[type] = tag;
};

// Register built-in components
registerComponent('ALERT', DynamicAlert);
registerComponent('BADGE', DynamicBadge);
registerComponent('BADGE_LIST', DynamicBadgeList);
registerComponent('BUTTON', DynamicButton);
registerComponent('CHECKBOX', DynamicCheckbox);
registerComponent('COL', DynamicGroup);
registerComponent('FIELDSET', DynamicFieldset);
registerComponent('FRAGMENT', DynamicGroup);
registerComponent('GROUP', DynamicGroup);
registerComponent('INLINE_GROUP', DynamicInlineGroup);
registerComponent('INPUT', DynamicInputResolver);
registerComponent('LABEL', DynamicLabel);
registerComponent('LIST', DynamicList);
registerComponent('RADIOBUTTON', DynamicRadioButton);
registerComponent('READONLY_FIELD', DynamicReadonlyField);
registerComponent('ROW', DynamicGroup);
registerComponent('SPACER', DynamicSpacer);
registerComponent('TEXTAREA', DynamicTextArea);

/**
 * Recursively renders a layout array by mapping each element's type to a registered component.
 * Unknown types are rendered as a fallback span.
 */
export default function renderLayout(content) {
    if (!content) return null;

    return content.map(({ type, key, ...props }) => {
        const Tag = components[type];
        if (!Tag) {
            return <span key={key} style={{ color: 'red' }}>Unknown type: {type}</span>;
        }
        return <Tag key={key} type={type} {...props} />;
    });
}
