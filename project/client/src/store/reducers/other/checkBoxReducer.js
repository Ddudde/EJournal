import {CHANGE_CHECKBOX} from '../../actions';

const initialState = {
    "0": false
};

export default function checkBoxReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_CHECKBOX:
            fd[action.payload.checkBoxId] = action.payload.checkBoxState;
            return fd;
        default:
            return state;
    }
}