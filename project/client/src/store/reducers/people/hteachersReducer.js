import {
    CHANGE_HTEACHERS,
    CHANGE_HTEACHERS_DEL,
    CHANGE_HTEACHERS_DEL_L2,
    CHANGE_HTEACHERS_EL_GL,
    CHANGE_HTEACHERS_GL,
    CHANGE_HTEACHERS_L2,
    CHANGE_HTEACHERS_L2_GL
} from '../../actions';

const initialState = {
        // "id1" : {
        //     name: "Школа",
        //     pep: {
        //         "id8" : {
        //             name: "Петров А.А."
        //         }
        //     }
        // },
        // "id2" : {
        //     name: "Гимназия"
        // },
        // "id3" : {
        //     name: "Лицей"
        // }
        // "id1" : {
        //     name: "Петров А.А."
        // },
        // "id2" : {
        //     name: "Петров А.С."
        // },
        // "id3" : {
        //     name: "Петров А.Г."
        // }
    };

export default function hteachersReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_HTEACHERS_GL:
            return action.payload.state;
        case CHANGE_HTEACHERS_L2:
            if(!fd[action.payload.l1].pep){
                fd[action.payload.l1].pep = {};
            }
            if(!fd[action.payload.l1].pep[action.payload.l2]){
                fd[action.payload.l1].pep[action.payload.l2] = {};
            }
            fd[action.payload.l1].pep[action.payload.l2][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_HTEACHERS_L2_GL:
            if(!fd[action.payload.l1].pep){
                fd[action.payload.l1].pep = {};
            }
            fd[action.payload.l1].pep[action.payload.l2] = action.payload.state;
            return fd;
        case CHANGE_HTEACHERS_DEL_L2:
            delete fd[action.payload.l1].pep[action.payload.l2];
            return fd;
        case CHANGE_HTEACHERS:
            if(!fd[action.payload.l1]){
                fd[action.payload.l1] = {};
            }
            fd[action.payload.l1][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_HTEACHERS_EL_GL:
            fd[action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_HTEACHERS_DEL:
            delete fd[action.payload.l1];
            return fd;
        default:
            return state;
    }
}