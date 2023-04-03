import {CHANGE_CLASSMATES, CHANGE_CLASSMATES_DEL, CHANGE_CLASSMATES_EL_GL, CHANGE_CLASSMATES_GL} from '../../actions';

const initialState = {
        // "id1" : {
        //     name: "Петров А.А."
        // },
        // "id2" : {
        //     name: "Васечкин А.С."
        // },
        // "id3" : {
        //     name: "Петров А.Г."
        // },
        // "id4" : {
        //     name: "Петров А.Г.1"
        // },
        // "id5" : {
        //     name: "Петров А.Г.2"
        // },
        // "id6" : {
        //     name: "Петров А.Г.3"
        // }
    };

export default function classmatesReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_CLASSMATES_GL:
            return action.payload.state;
        case CHANGE_CLASSMATES:
            if(!fd[action.payload.l1]){
                fd[action.payload.l1] = {};
            }
            fd[action.payload.l1][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_CLASSMATES_EL_GL:
            fd[action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_CLASSMATES_DEL:
            delete fd[action.payload.l1];
            return fd;
        default:
            return state;
    }
}