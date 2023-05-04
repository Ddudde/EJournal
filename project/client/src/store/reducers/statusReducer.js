import {CHANGE_STATE, CHANGE_STATE_GL, CHANGE_STATE_RESET} from '../actions';

const initialState = {
        auth: false,
        // invErr: false,
        // reaYes: false,
        // login: "nm13",
        // ico: 2,
        // role: 3,
        // uuid: "sdfds",
        // roles: true,
        // secFr: false,
        // roleDesc: "администратор портала",
        rolesDescrs: ["обучающийся", "родитель", "педагог", "завуч", "администратор портала"],
        // kid: "id1",
        // kids:{
        //     "id1": "Петров А.А.",
        //     "id2": "Петрова А.Б."
        // }
    };

export default function statusReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_STATE:
            fd[action.payload.stateId] = action.payload.cState;
            if(action.payload.stateId == "role") {
                fd.roleDesc = fd.rolesDescrs[action.payload.cState];
            }
            return fd;
        case CHANGE_STATE_GL:
            if(action.payload.cState){
                for(let el of Object.getOwnPropertyNames(action.payload.cState)){
                    fd[el] = action.payload.cState[el];
                    if(el == "role") {
                        fd.roleDesc = fd.rolesDescrs[action.payload.cState[el]];
                    }
                }
            }
            return fd;
        case CHANGE_STATE_RESET:
            return {
                auth: false,
                uuid: fd.uuid,
                rolesDescrs: ["обучающийся", "родитель", "педагог", "завуч", "администратор портала"],
            };
        default:
            return state;
    }
}