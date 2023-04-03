import {CHANGE_REQUEST, CHANGE_REQUEST_DEL, CHANGE_REQUEST_GL, CHANGE_REQUEST_PARAM} from '../actions';

const initialState = {
    // 0: {
    //     title: 'Мы перешли на этот сервис',
    //     date: '11.11.2022',
    //     text: 'Всем своим дружным коллективом мы остановились на данном варианте.'
    // }
};

export default function requestReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_REQUEST_GL:
            return action.payload.state;
        case CHANGE_REQUEST:
            fd[action.payload.id] = action.payload.state;
            return fd;
        case CHANGE_REQUEST_PARAM:
            fd[action.payload.id][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_REQUEST_DEL:
            delete fd[action.payload.id];
            return fd;
        default:
            return state;
    }
}