import {CHANGE_PERIODS, CHANGE_PERIODS_DEL, CHANGE_PERIODS_GL, CHANGE_PERIODS_L1} from '../../actions';

const initialState = {
    0 : {
        name: "I четверть",
        perN: "01.09.22",
        perK: "03.11.22"
    },
    1 : {
        name: "II четверть",
        perN: "12.11.22",
        perK: "29.12.22"
    },
    2 : {
        name: "III четверть",
        perN: "11.01.23",
        perK: "23.03.23"
    },
    3 : {
        name: "IV четверть",
        perN: "01.04.23",
        perK: "30.05.23"
    }
};

export default function periodsReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_PERIODS:
            if(!fd[action.payload.l1]){
                fd[action.payload.l1] = {};
            }
            fd[action.payload.l1][action.payload.l2] = action.payload.state;
            return fd;
        case CHANGE_PERIODS_GL:
            return action.payload.state;
        case CHANGE_PERIODS_L1:
            fd[action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_PERIODS_DEL:
            delete fd[action.payload.l1];
            return fd;
        default:
            return state;
    }
}