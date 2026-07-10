const Toast = {
    success: (msg) => { if(typeof showSuccessMessage === 'function') showSuccessMessage(msg); else console.log('SUCCESS:', msg); },
    error: (msg) => { if(typeof showErrorMessage === 'function') showErrorMessage(msg); else console.error('ERROR:', msg); },
    warning: (msg) => { console.warn('WARNING:', msg); },
    info: (msg) => { console.info('INFO:', msg); }
};