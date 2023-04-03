import {CHANGE_ADMINS, CHANGE_ADMINS_DEL, CHANGE_ADMINS_EL_GL, CHANGE_ADMINS_GL} from '../../actions';

const initialState = {
        // "id1" : {
        //     name: "Новиков А.А."
        // },
        // "id2" : {
        //     name: "Новиков А.С."
        // },
        // "id3" : {
        //     name: "Новиков А.Г."
        // }
    };

export default function adminsReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_ADMINS_GL:
            return action.payload.state;
        case CHANGE_ADMINS:
            if(!fd[action.payload.l1]){
                fd[action.payload.l1] = {};
            }
            fd[action.payload.l1][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_ADMINS_EL_GL:
            fd[action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_ADMINS_DEL:
            delete fd[action.payload.l1];
            return fd;
        default:
            return state;
    }
}