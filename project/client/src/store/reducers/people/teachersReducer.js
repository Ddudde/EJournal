import {CHANGE_TEACHERS, CHANGE_TEACHERS_DEL, CHANGE_TEACHERS_GL,} from '../../actions';

const initialState = {
    // 0 : {
    //     name: "Англ. яз.",
    //     tea: {
    //         "id1": {
    //             name: "Петров А.А.1"
    //             // link: sit + "/invite/x"
    //         },
    //         "id2": {
    //             name: "Петров А.Б.2"
    //         }
    //     }
    // },
    // 1: {
    //     name: "Русский яз.",
    //     tea: {
    //         "id1": {
    //             name: "Петров А.А.1"
    //         }
    //     }
    // },
    // 2: {
    //     name: "Математика",
    //     tea: {
    //         "id1": {
    //             name: "Петров А.А.1"
    //         }
    //     }
    // },
    // 3: {
    //     name: "Окруж. мир",
    //     tea: {
    //         "id1": {
    //             name: "Петров А.А.1"
    //         }
    //     }
    // },
    // nt : {
    //     tea: {
    //         "id5": {
    //             name: "Петров А.А.5"
    //         },
    //         "id6": {
    //             name: "Петров А.С.6"
    //         },
    //         "id7": {
    //             name: "Петров А.Г.7"
    //         }
    //     }
    // }
};

export default function teachersReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_TEACHERS_GL:
            return action.payload.state;
        case CHANGE_TEACHERS:
            if(!fd[action.payload.l0].tea[action.payload.l1]){
                fd[action.payload.l0].tea[action.payload.l1] = {};
            }
            fd[action.payload.l0].tea[action.payload.l1][action.payload.param] = action.payload.state;
            console.log(fd);
            return fd;
        case CHANGE_TEACHERS_DEL:
            delete fd[action.payload.l0].tea[action.payload.l1];
            return fd;
        default:
            return state;
    }
}