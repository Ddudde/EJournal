import {CHANGE_JOURNAL} from '../../actions';

const initialState = {
        "Англ. яз.": {
            days : {
                "10.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "16.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "17.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "18.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "19.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "20.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "21.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "22.02.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "10.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "16.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "17.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "18.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "19.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "20.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "21.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "22.03.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "01.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "03.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "04.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "05.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "06.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "07.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "08.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "09.04.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "14.04.22": {
                    mark: 2,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "14.05.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "15.05.22": {
                    mark: 5,
                    weight: 5,
                    type: "Контрольная работа"
                },
                "16.05.22": {
                    mark: "Н",
                    weight: 1
                },
                "17.05.22": {
                    mark: 3,
                    weight: 4,
                    type: "Самостоятельная работа"
                }
            },
            avg: {
                mark: "3.0"
            }
        },
        "Русский яз.": {
            days : {
                "16.05.22": {
                    mark: "Н",
                    weight: 1
                },
                "17.05.22": {
                    mark: 3,
                    weight: 4,
                    type: "Самостоятельная работа"
                },
            },
            avg: {
                mark: "3.0"
            }
        },
        "Математика": {
            days : {
                "14.05.22": {
                    mark: 5,
                    weight: 1,
                    type: "Ответ на уроке"
                },
                "16.05.22": {
                    mark: "Н",
                    weight: 1
                },
                "17.05.22": {
                    mark: "Н",
                    weight: 1
                },
            },
            avg: {
                mark: "3.0"
            }
        },
        "Окруж. мир": {
            days : {
                "14.05.22": {
                    mark: "Н",
                    weight: 1
                },
                "16.05.22": {
                    mark: "Н",
                    weight: 1
                },
                "17.05.22": {
                    mark: 3,
                    weight: 4,
                    type: "Самостоятельная работа"
                },
            },
            avg: {
                mark: "3.0"
            }
        }
};

export default function journalReducer(state = initialState, action) {
    switch(action.type) {
        case CHANGE_JOURNAL:
            return {
                    ...state,
                    [action.payload.jourId]: action.payload.jourState
                };
        default:
            return state;
    }
}