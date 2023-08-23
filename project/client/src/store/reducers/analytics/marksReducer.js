import {CHANGE_MARKS} from '../../actions';

const initialState = {
    namePers: {
        // 0: "I четверть",
        // 1: "II четверть",
        // 2: "III четверть",
        // 3: "IV четверть",
        // 4: "Годовая",
        // 5: "Итоговая"
    },
    jur: {
        // "Англ. яз.": {},
        // "Англ. яз.1": {},
        // "Англ. яз.2": {}
        // "Англ. яз.": {
        //     2: 2,
        //     3: 5,
        //     4: 5,
        //     5: 5
        // },
        // "Русский яз.": {
        //     0: 5,
        //     1: 4,
        //     2: 3,
        //     5: 5
        // }
    //     "Математика": {
    //         0: 5,
    //         1: 4,
    //         3: 2,
    //         4: 5,
    //         5: 3,
    //         6: 5
    //     },
    //     "Окруж. мир": {
    //         0: 5,
    //         1: 4,
    //         3: 2,
    //         4: 5,
    //         5: 5,
    //         6: 4
    //     }
    }
};

export default function marksReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_MARKS:
            fd[action.payload.id] = action.payload.state;

            return fd;
        default:
            return state;
    }
}