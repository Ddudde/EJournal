import {CHANGE_SCHEDULE, CHANGE_SCHEDULE_DEL, CHANGE_SCHEDULE_GL, CHANGE_SCHEDULE_PARAM} from '../../actions';

const initialState = {
    0 : {
        dayId: undefined,
        lessons: {
            0: {
                name: "Англ. яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            1 : {
                name: "Русский яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            2 : {
                name: "Математика",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            3 : {
                name: "Окруж. мир",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            }
        }
    },
    1 : {
        dayId: undefined,
        lessons: {
            0 : {
                name: "Русский яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            1 : {
                name: "Математика",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            2 : {
                name: "Англ. яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            3 : {
                name: "Русский яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            4 : {
                name: "Математика",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            5 : {
                name: "Окруж. мир",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            }
        }
    },
    2 : {
        dayId: undefined,
        lessons: {
            0 : {
                name: "Англ. яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            1 : {
                name: "Англ. яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            2 : {
                name: "Русский яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            3 : {
                name: "Математика",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            4 : {
                name: "Окруж. мир",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            }
        }
    },
    3 : {
        dayId: undefined,
        lessons: {
            0 : {
                name: "Математика",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            1 : {
                name: "Окруж. мир",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            }
        }
    },
    4 : {
        dayId: undefined,
        lessons: {
            0 : {
                name: "Англ. яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            },
            1 : {
                name: "Русский яз.",
                cabinet: "300",
                prepod: {
                    name: "Петренко А.А.",
                    id: "id1"
                },
                group: "10A"
            }
        }
    },
    5 : {dayId: undefined,
        lessons: {}},
    6 : {dayId: undefined,
        lessons: {}}
};

export default function scheduleReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_SCHEDULE_PARAM:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            if(!fd[action.payload.l0].lessons[action.payload.l1]){
                fd[action.payload.l0].lessons[action.payload.l1] = {};
            }
            fd[action.payload.l0].lessons[action.payload.l1][action.payload.l2] = action.payload.state;
            return fd;
        case CHANGE_SCHEDULE_GL:
            if(!action.payload.state) action.payload.state = {};
            return action.payload.state;
        case CHANGE_SCHEDULE:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            fd[action.payload.l0].lessons[action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_SCHEDULE_DEL:
            delete fd[action.payload.l0].lessons[action.payload.l1];
            return fd;
        default:
            return state;
    }
}