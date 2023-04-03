import {CHANGE_PROFILE, CHANGE_PROFILE_GL, CHANGE_PROFILE_ROLES} from "../../actions";

const initialState = {
    // login: "test",
    // ico: 2,
    // fio: "Иванов Иван Иванович",
    // more: "",
    // roles: {
    //     0: {
    //         yo: "Школа №1541",
    //         email: "ya@ya.ru",
    //         parents: {
    //             "id1": "Петров А.А.",
    //             "id2": "Петрова А.Б."
    //         },
    //         group: "10A"
    //     },
    //     1: {
    //         yo: "Школа №1541",
    //         kids: {
    //             "id1": "Петров А.А.",
    //             "id2": "Петрова А.Б."
    //         },
    //         email: "ya@ya.ru"
    //     },
    //     2: {
    //         yo: "Школа №1541",
    //         lessons: ["Англ. Яз.", "Математика"],
    //         email: "ya@ya.ru"
    //     },
    //     3: {
    //         yo: "Школа №1541",
    //         email: "ya@ya.ru"
    //     },
    //     4: {
    //         email: "ya@ya.ru",
    //     }
    // }
};

export default function profileReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_PROFILE_GL:
            return action.payload.State;
        case CHANGE_PROFILE:
            fd[action.payload.Id] = action.payload.State
            return fd;
        case CHANGE_PROFILE_ROLES:
            fd.roles[action.payload.roleId][action.payload.Id] = action.payload.State;
            return fd;
        default:
            return state;
    }
}