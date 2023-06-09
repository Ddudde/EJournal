import {CHANGE_DNEVNIK, CHANGE_DNEVNIK_DAY_DOWN, CHANGE_DNEVNIK_DAY_UP} from '../actions';

const initialState = {
    min: "25.05.23",
    max: "01.07.23",
    reqWeek: [0],
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
    let fd = {...state};
    switch(action.type) {
        case CHANGE_DNEVNIK:
            fd[action.payload.stateId] = action.payload.cState;
            return fd;
        case CHANGE_DNEVNIK_DAY_UP:
            return fd;
        case CHANGE_DNEVNIK_DAY_DOWN:
            return fd;
        default:
            return state;
    }
}