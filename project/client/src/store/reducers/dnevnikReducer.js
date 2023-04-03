import {CHANGE_DNEVNIK, CHANGE_DNEVNIK_DAY_DOWN, CHANGE_DNEVNIK_DAY_UP} from '../actions';

const initialState = {
    currWeek: "14.05.22",
    min: "09.05.22",
    max: "23.05.22",
    schedule: {
        0 : ["Англ. яз.", "Русский яз.", "Математика", "Окруж. мир"],
        1 : ["Русский яз.", "Математика", "Англ. яз.", "Русский яз.", "Математика", "Окруж. мир"],
        2 : ["Англ. яз.", "Англ. яз.", "Русский яз.", "Математика", "Окруж. мир"],
        3 : ["Математика", "Окруж. мир"],
        4 : ["Англ. яз.", "Русский яз."],
        5 : [],
        6 : []
    },
    days: {
        "14.05.22": {
            lessons: [
                {
                    homework: "Упр. 5Стр. 103,Упр. 2Стр. 104",
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                {
                    homework: "Стр. 18Упр. 328"
                },
                {
                    homework: "Стр. 36 №5, стр.37 N09"
                },
                {
                    homework: "Стр. 62-63 пересказ"
                }
            ]
        },
        "15.05.22": {
            lessons: [
                {
                    homework: "Стр. 18Упр. 328"
                },
                {
                    homework: "Стр. 36 №5, стр.37 N09"
                },
                {
                    homework: "Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104Упр. 5Стр. 103,Упр. 2Стр. 104",
                    mark: 5,
                    weight: 2,
                    type: "Тест"
                },
                {
                    homework: "Стр. 18Упр. 328"
                },
                {
                    homework: "Стр. 36 №5, стр.37 N09"
                },
                {
                    homework: "Стр. 62-63 пересказ"
                }
            ]
        },
        "16.05.22": {
            lessons: [
                {
                    homework: "Упр. 5Стр. 103,Упр. 2Стр. 104",
                    mark: 5,
                    weight: 1
                },
                {
                    homework: "Упр. 5Стр. 103,Упр. 2Стр. 104",
                    mark: 5,
                    weight: 1
                },
                {
                    homework: "Стр. 18Упр. 328"
                },
                {
                    homework: "Стр. 36 №5, стр.37 N09"
                },
                {
                    homework: "Стр. 62-63 пересказ"
                }
            ]
        },
        "17.05.22": {
            lessons: [
                {
                    homework: "Стр. 36 №5, стр.37 N09"
                },
                {
                    homework: "Стр. 62-63 пересказ"
                }
            ]
        },
        "18.05.22": {
            lessons: [
                {
                    homework: "Упр. 5Стр. 103,Упр. 2Стр. 104",
                    mark: 5,
                    weight: 1
                },
                {
                    homework: "Стр. 18Упр. 328"
                }
            ]
        },
        "19.05.22": {
            lessons: []
        },
        "20.05.22": {
            lessons: []
        }
    }
};

export default function dnevnikReducer(state = initialState, action) {
    switch(action.type) {
        case CHANGE_DNEVNIK:
            return {
                    ...state,
                    [action.payload.stateId]: action.payload.cState
                };
        case CHANGE_DNEVNIK_DAY_UP:
            return {
                    ...state,
                    days: {
                        [action.payload.stateId]: action.payload.cState,
                        ...state.days
                    }
                };
        case CHANGE_DNEVNIK_DAY_DOWN:
            return {
                ...state,
                days: {
                    ...state.days,
                    [action.payload.stateId]: action.payload.cState
                }
            };
        default:
            return state;
    }
}