import {CHANGE_MARKS} from '../../actions';

const initialState = {
        namePers: [
            "I", "II", "III", "IV"
        ],
        pers: {
            "Англ. яз.": {
                per: {
                    "I": 5,
                    "II": 4,
                    "III": 2,
                    "IV": 5
                },
                year: 5,
                itog: 5
            },
            "Русский яз.": {
                per: {
                    "I": 5,
                    "II": 4,
                    "III": 3,
                    "IV": 5
                },
                year: 5,
                itog: 5
            },
            "Математика": {
                per: {
                    "I": 5,
                    "II": 4,
                    "III": 2,
                    "IV": 5
                },
                year: 3,
                itog: 5
            },
            "Окруж. мир": {
                per: {
                    "I": 5,
                    "II": 4,
                    "III": 2,
                    "IV": 5
                },
                year: 5,
                itog: 4
            }
        }
};

export default function marksReducer(state = initialState, action) {
    switch(action.type) {
        case CHANGE_MARKS:
            return {
                    ...state,
                    [action.payload.markId]: action.payload.markState
                };
        default:
            return state;
    }
}