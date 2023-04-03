import {
    CHANGE_PJOURNAL,
    CHANGE_PJOURNAL_DEL_MARKS,
    CHANGE_PJOURNAL_DEL_PER_MARKS,
    CHANGE_PJOURNAL_DEL_TYPE,
    CHANGE_PJOURNAL_DZ,
    CHANGE_PJOURNAL_MARKS,
    CHANGE_PJOURNAL_NEW_TYPE,
    CHANGE_PJOURNAL_PER_MARKS,
    CHANGE_PJOURNAL_TYPE
} from '../actions';

const initialState = {
        predm: 1,
        predms: {
            0: "Информатика",
            1: "Математика",
            2: "Русский яз."
        },
        mar: 0,
        pers: ["I", "Годовая", "Итоговая"],
        typs: {
            "Ответ на уроке": 1,
            "Самостоятельная работа": 4,
            "Контрольная работа": 5
        },
        typ: "",
        jur: {
            day: {
                0 : "10.02.22",
                1 : "16.02.22",
                2 : "17.02.22",
                3 : "18.02.22",
                4 : "19.02.22",
                5 : "20.02.22",
                6 : "21.02.22",
                7 : "22.02.22",
                8 : "10.03.22",
                9 : "16.03.22",
                10 : "17.03.22",
                11 : "18.03.22",
                12 : "19.03.22",
                13 : "20.03.22",
                14 : "21.03.22",
                15 : "22.03.22",
                16 : "01.04.22",
                17 : "03.04.22",
                18 : "04.04.22",
                19 : "05.04.22",
                20 : "06.04.22",
                21 : "07.04.22",
                22 : "08.04.22",
                23 : "09.04.22",
                24 : "14.04.22",
                25 : "14.05.22",
                26 : "15.05.22",
                27 : "16.05.22",
                28 : "17.05.22",
                29 : "18.05.22"
            },
            kids: {
                'Петров А.А.': {
                    days: {
                        0: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        1: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        2: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        3: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        4: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        5: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        6: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        7: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        8: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        9: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        10: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        11: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        12: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        13: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        14: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        15: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        16: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        17: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        18: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        19: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        20: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        21: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        22: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        23: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        24: {
                            mark: 2,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        25: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        26: {
                            mark: 5,
                            weight: 5,
                            type: "Контрольная работа"
                        },
                        27: {
                            mark: "Н",
                            weight: 1
                        },
                        28: {
                            mark: 3,
                            weight: 4,
                            type: "Самостоятельная работа"
                        }
                    },
                    avg: {
                        mark: "3.04",
                        I: 2
                    }
                },
                'Петрова А.Б.': {
                    days: {
                        15: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        27: {
                            mark: "Н",
                            weight: 1
                        },
                        28: {
                            mark: 3,
                            weight: 4,
                            type: "Самостоятельная работа"
                        }
                    },
                    avg: {
                        mark: "3.0"
                    }
                },
                'Петрова А.В.': {
                    days: {
                        25: {
                            mark: 5,
                            weight: 1,
                            type: "Ответ на уроке"
                        },
                        27: {
                            mark: "Н",
                            weight: 1
                        },
                        28: {
                            mark: "Н",
                            weight: 1
                        }
                    },
                    avg: {
                        mark: "3.0"
                    }
                },
                'Петрова А.Г.': {
                    days: {
                        25: {
                            mark: "Н",
                            weight: 1
                        },
                        27: {
                            mark: "Н",
                            weight: 1
                        },
                        28: {
                            mark: 3,
                            weight: 4,
                            type: "Самостоятельная работа"
                        }
                    },
                    avg: {
                        mark: "3.0"
                    }
                }
            }
        },
    dz:{
        0: "Дз№1",
        20: "Дз№2",
        27: "Дз№23"
    }
};

export default function pjournalReducer(state = initialState, action) {
    let fd;
    switch(action.type) {
        case CHANGE_PJOURNAL:
            return {
                    ...state,
                    [action.payload.Id]: action.payload.State
                };
        case CHANGE_PJOURNAL_MARKS:
            fd = {...state};
            fd.jur.kids[action.payload.kid].days[action.payload.day] = action.payload.State;
            return fd;
        case CHANGE_PJOURNAL_DEL_MARKS:
            fd = {...state};
            delete fd.jur.kids[action.payload.kid].days[action.payload.day];
            return fd;
        case CHANGE_PJOURNAL_PER_MARKS:
            fd = {...state};
            fd.jur.kids[action.payload.kid].avg[action.payload.per] = action.payload.State;
            return fd;
        case CHANGE_PJOURNAL_DEL_PER_MARKS:
            fd = {...state};
            delete fd.jur.kids[action.payload.kid].avg[action.payload.per];
            return fd;
        case CHANGE_PJOURNAL_TYPE:
            fd = {...state};
            fd.typs = JSON.parse(JSON.stringify(fd.typs).replaceAll(action.payload.pret, action.payload.t));
            fd.typs[action.payload.t] = action.payload.st;
            return fd;
        case CHANGE_PJOURNAL_DEL_TYPE:
            fd = {...state};
            delete fd.typs[action.payload.t];
            return fd;
        case CHANGE_PJOURNAL_NEW_TYPE:
            fd = {...state};
            fd.typs[action.payload.t] = action.payload.st;
            return fd;
        case CHANGE_PJOURNAL_DZ:
            fd = {...state};
            fd.dz[action.payload.dz] = action.payload.st;
            return fd;
        default:
            return state;
    }
}