import React from 'react';

/**
 * DynamicLayoutContext provides data, UI description, and actions
 * to all DynamicLayout components in the tree.
 */
const DynamicLayoutContext = React.createContext({
    ui: { title: '', layout: [], translations: {}, userAccess: {} },
    data: {},
    setData: () => {},
    setVariables: () => {},
    variables: {},
    validationErrors: [],
    isFetching: false,
    callAction: () => {},
    renderLayout: () => null,
    options: {},
});

export default DynamicLayoutContext;
