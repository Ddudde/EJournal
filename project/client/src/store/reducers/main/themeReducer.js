import {CHANGE_THEME} from '../../actions';

let x = !window.matchMedia('(prefers-color-scheme: dark)');

const initialState = {
    theme_ch: x,
    theme: x ? "светлая" : "тёмная",
    thP: {
        true: {
            c: "theme_light",
            p: "theme_dark",
            params: {
                "--bgcV1": "#DBDBDBe6",
                "--bgcV2": "#242424e6",
                "--bgcV3": "#000000b3",
                "--shdV1": "#fff",
                "--shdV2": "#000",
                "--cV1": "#006600",
                "--cV2": "#009900",
                "--cV3": "#090a0b",
                "--bcV1": "#4d4d4d",
                "--bcV2": "#090a0b",
            }
        },
        false: {
            c: "theme_dark",
            p: "theme_light",
            params: {
                "--bgcV1": "#242424e6",
                "--bgcV2": "#DBDBDBe6",
                "--bgcV3": "#0000004d",
                "--shdV1": "#000",
                "--shdV2": "#fff",
                "--cV1": "#009900",
                "--cV2": "#00bb00",
                "--cV3": "#f5f6f7",
                "--bcV1": "#b3b3b3",
                "--bcV2": "#f5f6f7",
            }
        }
    }
};

export default function themeReducer(state = initialState, action) {
    let fd = {...state};
    switch(action.type) {
        case CHANGE_THEME:
            fd.theme_ch = action.payload;
            fd.theme = action.payload ? "светлая" : "тёмная";
            return fd;
        default:
            return state;
    }
}