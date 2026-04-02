import {store} from './store';

describe('store', () => {
    it('should initialize states', () => {
        const state = store.getState();

        expect(state).toHaveProperty('auth');
        expect(state).toHaveProperty('notes');
    });
});