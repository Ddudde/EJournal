import {CHANGE_PANE, CHANGE_PANE_DEL_GRS, CHANGE_PANE_GR, CHANGE_PANE_GRS} from '../../actions';

const initialState = {
    els: []
};

export default function paneReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_PANE:
            fd.els[action.payload.id] = action.payload.state;
            return fd;
        case CHANGE_PANE_GR:
            fd.els[action.payload.id].group = action.payload.state;
            return fd;
        case CHANGE_PANE_GRS:
            fd.els[action.payload.id].groups[action.payload.gId] = action.payload.state;
            fd.els[action.payload.id].group = action.payload.gId;
            return fd;
        case CHANGE_PANE_DEL_GRS:
            delete fd.els[action.payload.id].groups[action.payload.gId];
            return fd;
        default:
            return state;
    }
}