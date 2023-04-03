import {
    CHANGE_ZVONKI,
    CHANGE_ZVONKI_DEL,
    CHANGE_ZVONKI_DEL_L0,
    CHANGE_ZVONKI_L1,
    CHANGE_ZVONKI_SMENA
} from '../../actions';

const initialState = {
        0 : {
            name: "1 смена",
            lessons: {
                0: "8.00-8.45",
                1: "8.50-9.35",
                2: "9.45-10.30",
                3: "10.40-11.25",
                4: "11.30-12.15",
                5: "12.20-13.05",
                6: "13.10-13.55"
            }
        },
        1 : {
            name: "2 смена",
            lessons: {
                0: "13.10-13.55",
                1: "14.00-14.45",
                2: "14.55-15.40",
                3: "15.50-16.35",
                4: "16.40-17.25",
                5: "17.30-18.15",
                6: "18.20-19.05"
            }
        }
    };

export default function zvonkiReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_ZVONKI:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            if(!fd[action.payload.l0][action.payload.l1]){
                fd[action.payload.l0][action.payload.l1] = {};
            }
            fd[action.payload.l0][action.payload.l1][action.payload.l2] = action.payload.state;
            return fd;
        case CHANGE_ZVONKI_SMENA:
            fd[action.payload.l0] = action.payload.state;
            return fd;
        case CHANGE_ZVONKI_L1:
            if(!fd[action.payload.l0]){
                fd[action.payload.l0] = {};
            }
            fd[action.payload.l0][action.payload.l1] = action.payload.state;
            return fd;
        case CHANGE_ZVONKI_DEL:
            delete fd[action.payload.l0][action.payload.l1][action.payload.l2];
            return fd;
        case CHANGE_ZVONKI_DEL_L0:
            delete fd[action.payload.l0];
            return fd;
        default:
            return state;
    }
}