import {CHANGE_DIALOG, CHANGE_DIALOG_BUT, CHANGE_DIALOG_DEL} from '../../actions';

const initialState = {
    // obj: {}
    // buts: {
    //     0 : {
    //         text: "Прочитал"
    //     }
    // }
};

export default function dialogReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_DIALOG:
            return action.payload.state;
        case CHANGE_DIALOG_BUT:
            if(!fd.buts) fd.buts = {};
            if(!fd.buts[action.payload.id]) {
                fd.buts[action.payload.id] = {};
            }
            fd.buts[action.payload.id].enab = action.payload.state;
            return fd;
        case CHANGE_DIALOG_DEL:
            return {};
        default:
            return state;
    }
}