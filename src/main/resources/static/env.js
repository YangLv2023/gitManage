/*
 * Runtime environment config (no build step, plain script include).
 *
 * Resolution order used by the pages:
 * 1. Local startup (page opened from localhost / 127.0.0.1 / file://):
 *    always talks to http://localhost:5555, this file is ignored.
 * 2. Otherwise the value below is used (deployed intranet backend).
 *
 * Deploy to another environment: just edit API_BASE here, no page changes needed.
 */
window.ENV = {
    //API_BASE: 'http://192.168.11.70:5555'
    API_BASE: 'http://localhost:5555'
};
