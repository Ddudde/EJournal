import {CHANGE_PERIODS, CHANGE_PERIODS_DEL, CHANGE_PERIODS_L1} from '../../actions';

const initialState = {
    prs: {
        0 : {
            name: "I четверть",
            per: "01.09.22-03.11.22"
        },
        1 : {
            name: "II четверть",
            per: "12.11.22-29.12.22"
        },
        2 : {
            name: "III четверть",
            per: "11.01.23-23.03.23"
        },
        3 : {
            name: "IV четверть",
            per: "01.04.23-30.05.23"
        }
    }
};

export default function periodsReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_PERIODS:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            if(!fd[action.payload.l0][action.payload.l1]){
                fd[action.payload.l0][action.payload.l1] = {};
            }
            fd[action.payload.l0][action.payload.l1][action.payload.l2] = action.payload.state;
            return fd;
        case CHANGE_PERIODS_L1:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            fd[action.payload.l0][action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_PERIODS_DEL:
            delete fd[action.payload.l0][action.payload.l1];
            return fd;
        default:
            return state;
    }
}