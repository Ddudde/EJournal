import {
    CHANGE_PARENTS,
    CHANGE_PARENTS_DEL,
    CHANGE_PARENTS_DEL_L0,
    CHANGE_PARENTS_DEL_L1,
    CHANGE_PARENTS_GL,
    CHANGE_PARENTS_L1,
    CHANGE_PARENTS_L1_PARAM
} from '../../actions';

const initialState = {
    nw : {
        name : "Фамилия И.О."
        // par : {
        //     "id1": {
        //         name : "Петров А.А."
        //     },
        //     "id2": {
        //         name : "Петрова А.Б."
        //     }
        // }
    }
    // "id1" : {
    //     name : "Петров А.А.",
    //     par : {
    //         "id1": {
    //             name : "Петров А.А."
    //         },
    //         "id2": {
    //             name : "Петрова А.Б."
    //         }
    //     }
    // },
    // "id2": {
    //     name : "Васечкин А.С.",
    //     par : {
    //         "id1": {
    //             name : "Петров А.А."
    //         }
    //     }
    // },
    // "id3": {
    //     name : "Петров А.Г.",
    //     par : {
    //         "id1": {
    //             name : "Петров А.А."
    //         }
    //     }
    // }
};

export default function parentsReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_PARENTS_GL:
            let nw = {...fd.nw};
            action.payload.state.nw = nw;
            return action.payload.state;
        case CHANGE_PARENTS_L1_PARAM:
            if(!fd[action.payload.l1]){
                fd[action.payload.l1] = {};
            }
            fd[action.payload.l1][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_PARENTS_L1:
            fd[action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_PARENTS:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            if(!fd[action.payload.l0][action.payload.l1]){
                fd[action.payload.l0][action.payload.l1] = {};
            }
            if(!fd[action.payload.l0][action.payload.l1][action.payload.l2]){
                fd[action.payload.l0][action.payload.l1][action.payload.l2] = {};
            }
            fd[action.payload.l0][action.payload.l1][action.payload.l2][action.payload.param] = action.payload.state;
            return fd;
        case CHANGE_PARENTS_DEL:
            delete fd[action.payload.l0][action.payload.l1][action.payload.l2];
            return fd;
        case CHANGE_PARENTS_DEL_L1:
            delete fd[action.payload.l0][action.payload.l1];
            return fd;
        case CHANGE_PARENTS_DEL_L0:
            delete fd[action.payload.l0];
            return fd;
        default:
            return state;
    }
}