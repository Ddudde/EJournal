import {createStore} from 'redux';
import rootReducer from './rootReducer';

export const createAppStore = createStore(rootReducer);

export default createAppStore;