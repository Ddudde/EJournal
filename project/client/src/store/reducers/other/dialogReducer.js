import {CHANGE_DIALOG, CHANGE_DIALOG_DEL} from '../../actions';

const initialState = {
    // obj: {}
    // buts: {
    //     0 : {
    //         text: "Прочитал"
    //     }
    // }
};

export default function dialogReducer(state = initialState, action) {
    switch(action.type) {
        case CHANGE_DIALOG:
            return action.payload;
        case CHANGE_DIALOG_DEL:
            return {};
        default:
            return state;
    }
}