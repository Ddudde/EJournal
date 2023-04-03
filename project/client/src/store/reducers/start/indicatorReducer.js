import {CHANGE_INDICATOR} from '../../actions';

const initialState = {
    actived: 0
};

export default function indicatorReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_INDICATOR:
            fd.actived = action.payload;
            return fd;
        default:
            return state;
    }
}