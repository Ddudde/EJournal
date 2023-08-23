import {
    CHANGE_PJOURNAL,
    CHANGE_PJOURNAL_DEL_MARKS,
    CHANGE_PJOURNAL_DEL_PER_MARKS,
    CHANGE_PJOURNAL_DEL_TYPE,
    CHANGE_PJOURNAL_DZ,
    CHANGE_PJOURNAL_GL,
    CHANGE_PJOURNAL_MARKS,
    CHANGE_PJOURNAL_NEW_TYPE,
    CHANGE_PJOURNAL_PER_MARKS,
    CHANGE_PJOURNAL_TYPE
} from '../actions';

const initialState = {
    // min: "25.06.23",
    // max: "01.10.23",
    predm: 0,
    // predms: {
    //     0: "Информатика",
    //     1: "Математика",
    //     2: "Русский яз."
    // },
    mar: 0,
    // pers: {
    //     0: "I",
    //     1: "Годовая",
    //     2: "Итоговая"
    // },
    typs: {
        "Ответ на уроке": 1,
        "Самостоятельная работа": 4,
        "Контрольная работа": 5
    },
    typ: "",
    jur: {
        // 0: {
        //     name: 'Петров А.А.',
        //     days: {
        //         "10.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "16.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "17.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "18.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "19.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "20.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "21.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "23.02.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "10.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "16.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "17.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "18.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "19.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "20.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "21.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "23.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "01.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "03.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "04.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "05.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "06.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "07.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "08.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "09.04.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "14.04.23": {
        //             mark: 2,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "14.05.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "15.05.23": {
        //             mark: 5,
        //             weight: 5,
        //             type: "Контрольная работа"
        //         },
        //         "16.05.23": {
        //             mark: "Н",
        //             weight: 1
        //         },
        //         "16.05.23,0": {
        //             mark: "3",
        //             weight: 1
        //         },
        //         "17.05.23": {
        //             mark: 3,
        //             weight: 4,
        //             type: "Самостоятельная работа"
        //         }
        //     },
        //     avg: {
        //         mark: "3.04",
        //         0: 2
        //     }
        // },
        // 1: {
        //     name: 'Петрова А.Б.',
        //     days: {
        //         "23.03.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "16.05.23": {
        //             mark: "Н",
        //             weight: 1
        //         },
        //         "17.05.23": {
        //             mark: 3,
        //             weight: 4,
        //             type: "Самостоятельная работа"
        //         }
        //     },
        //     avg: {
        //         mark: "3.0"
        //     }
        // },
        // 2: {
        //     name: 'Петрова А.В.',
        //     days: {
        //         "14.05.23": {
        //             mark: 5,
        //             weight: 1,
        //             type: "Ответ на уроке"
        //         },
        //         "16.05.23": {
        //             mark: "Н",
        //             weight: 1
        //         },
        //         "17.05.23": {
        //             mark: "Н",
        //             weight: 1
        //         }
        //     },
        //     avg: {
        //         mark: "3.0"
        //     }
        // },
        // 3: {
        //     name: 'Петрова А.Г.',
        //     days: {
        //         "14.05.23": {
        //             mark: "Н",
        //             weight: 1
        //         },
        //         "16.05.23": {
        //             mark: "Н",
        //             weight: 1
        //         },
        //         "17.05.23": {
        //             mark: 3,
        //             weight: 4,
        //             type: "Самостоятельная работа"
        //         }
        //     },
        //     avg: {
        //         mark: "3.0"
        //     }
        // }
    },
    // dz:{
    //     '15.08.23': "Дз№1g",
    //     '15.08.23,0': "Дз№1g5",
    //     '15.08.23,1': "Дз№1g56",
    //     '16.08.23': "Дз№1",
    //     '17.08.23': "Дз№2",
    //     '18.08.23': "Дз№23"
    // }
};

export default function pjournalReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_PJOURNAL:
            fd[action.payload.id] = action.payload.state;
            return fd;
        case CHANGE_PJOURNAL_GL:
            fd.jur = action.payload.state;
            return fd;
        case CHANGE_PJOURNAL_MARKS:
            // action.payload.state.mark = fd.mar;
            // if(fd.typ != "") action.payload.state.type = fd.typ;
            fd.jur[action.payload.kid].days[action.payload.day] = action.payload.state;
            return fd;
        case CHANGE_PJOURNAL_DEL_MARKS:
            delete fd.jur[action.payload.kid].days[action.payload.day];
            return fd;
        case CHANGE_PJOURNAL_PER_MARKS:
            fd.jur[action.payload.kid].avg[action.payload.per] = action.payload.state.mark;
            return fd;
        case CHANGE_PJOURNAL_DEL_PER_MARKS:
            delete fd.jur[action.payload.kid].avg[action.payload.per];
            return fd;
        case CHANGE_PJOURNAL_TYPE:
            fd.typs = JSON.parse(JSON.stringify(fd.typs).replaceAll(action.payload.pret, action.payload.t));
            fd.typs[action.payload.t] = action.payload.st;
            return fd;
        case CHANGE_PJOURNAL_DEL_TYPE:
            delete fd.typs[action.payload.t];
            return fd;
        case CHANGE_PJOURNAL_NEW_TYPE:
            fd.typs[action.payload.t] = action.payload.st;
            return fd;
        case CHANGE_PJOURNAL_DZ:
            fd.dz[action.payload.dz] = action.payload.st;
            return fd;
        default:
            return state;
    }
}