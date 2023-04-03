import React, {useEffect, useRef} from "react";
import analyticsCSS from './analyticsMain.module.css';
import {Outlet} from "react-router-dom";
import {pane, states} from "../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import Pane from "../other/pane/Pane";
import {setActived} from "../main/Main";
import {
    CHANGE_PERIODS,
    CHANGE_PERIODS_DEL,
    CHANGE_PERIODS_L1,
    CHANGE_ZVONKI,
    CHANGE_ZVONKI_DEL,
    CHANGE_ZVONKI_DEL_L0,
    CHANGE_ZVONKI_L1,
    CHANGE_ZVONKI_SMENA,
    changeAnalytics
} from "../../store/actions";

let gr, cState, ke, dispatch;

gr = {
    group: 0
}

export function onDel(e, type) {
    let par, inp, id;
    par = e.target.parentElement.parentElement;
    if(par.classList.contains(analyticsCSS.edbl)){
        inp = par.querySelector("input");
        if (inp.hasAttribute("data-id")) {
            id = inp.getAttribute("data-id").split("_");
            if(type == CHANGE_ZVONKI_DEL) {
                dispatch(changeAnalytics(type, id[0], "lessons", id[1]));
            } else if(type == CHANGE_PERIODS_DEL) {
                dispatch(changeAnalytics(type, "prs", id[0]));
            }
        } else if(inp.hasAttribute("data-id1")){
            let id = inp.getAttribute("data-id1");
            if(type == CHANGE_ZVONKI_DEL_L0) {
                dispatch(changeAnalytics(type, id));
            }
        }
    }
}

export function onEdit(e) {
    let par = e.target.parentElement;
    if(par.classList.contains(analyticsCSS.add)){
        par.setAttribute('data-st', '1');
    }
    if(par.parentElement.classList.contains(analyticsCSS.edbl)){
        par = par.parentElement;
        par.setAttribute('data-st', '1');
    }
}

export function onFin(e, inps, forceUpdate, type, info) {
    let par, inp;
    par = e.target.parentElement;
    inp = par.querySelector("input");
    if(par.classList.contains(analyticsCSS.edbl)){
        if(type == CHANGE_PERIODS_L1){
            let inpm = ["inpnnt_", "inpnit_"];
            if(inps.inpnnt_ && inps.inpnit_)
            {
                let grop, id, obj;
                grop = Object.getOwnPropertyNames(info.prs);
                id = grop.length == 0 ? 0 : (parseInt(grop[grop.length-1]) + 1);
                obj = {
                    name: inps.inpnnt_,
                    per: inps.inpnit_
                }
                dispatch(changeAnalytics(type, "prs", id, undefined, obj));
            } else {
                for(let i = 0, inpf; i < inpm.length; i++) {
                    inpf = document.querySelector("." + analyticsCSS.edbl + " *[id='" + inpm[i] + "']")
                    inpf.setAttribute("data-mod", '1');
                }
            }
            return;
        }
    }
    if (inps[inp.id]) {
        inp.setAttribute("data-mod", '0');
        if(par.parentElement.classList.contains(analyticsCSS.edbl)) {
            par = par.parentElement;
            if(type){
                if(inp.hasAttribute("data-id")){
                    let id = inp.getAttribute("data-id").split("_");
                    if(type == CHANGE_ZVONKI) {
                        dispatch(changeAnalytics(type, id[0], "lessons", id[1], inp.value));
                    } else if(type == CHANGE_PERIODS) {
                        dispatch(changeAnalytics(type, "prs", id[0], id[1], inp.value));
                    }
                } else if(inp.hasAttribute("data-id1")){
                    let id = inp.getAttribute("data-id1");
                    if(type == CHANGE_ZVONKI_L1) {
                        dispatch(changeAnalytics(type, id, "name", undefined, inp.value));
                    }
                }
            } else {
                inps.inpnpt = inp.value;
                forceUpdate();
            }
        } else if(par.classList.contains(analyticsCSS.edbl)) {
            if(inp.hasAttribute("data-id1")) {
                let id = inp.getAttribute("data-id1");
                if (type == CHANGE_ZVONKI) {
                    let grop, id1;
                    grop = info[id] && info[id].lessons ? Object.getOwnPropertyNames(info[id].lessons) : [];
                    id1 = grop.length == 0 ? 0 : (parseInt(grop[grop.length-1]) + 1);
                    dispatch(changeAnalytics(type, id, "lessons", id1, inp.value));
                }
            } else if(type == CHANGE_ZVONKI_SMENA){
                let grop, id, obj;
                grop = Object.getOwnPropertyNames(info);
                id = grop.length == 0 ? 0 : (parseInt(grop[grop.length-1]) + 1);
                obj = {
                    name: inp.value
                }
                dispatch(changeAnalytics(type, id, undefined, undefined, obj));
            }
        }
        par.setAttribute('data-st', '0');
    } else {
        inp.setAttribute("data-mod", '1');
    }
}

export function onClose(e, type) {
    let par = e.target.parentElement;
    if(par.parentElement.classList.contains(analyticsCSS.edbl)){
        if(par.classList.contains(analyticsCSS.fi) || type) {
            par = par.parentElement.parentElement;
        } else {
            par = par.parentElement;
        }
        par.setAttribute('data-st', '0');
    } else if(par.classList.contains(analyticsCSS.edbl)) {
        par = par.parentElement;
        par.setAttribute('data-st', '0');
    }
}

export function chStatB(e, inps, upd) {
    let el = e.target;
    inps[el.id] = !el.validity.patternMismatch ? el.value : false;
    if (inps[el.id]) {
        el.setAttribute("data-mod", '0');
    } else {
        el.setAttribute("data-mod", '1');
    }
    if(upd) upd();
    let ye = el.parentElement.querySelector(".yes");
    if(ye) {
        ye.setAttribute("data-enable", +inps[el.id]);
    }
}

export function ele (x, par, inps) {
    if(!inps[par]) inps[par] = x;
}

export function setActNew(name) {
    gr.group = name;
}

export function AnalyticsMain(props) {
    cState = useSelector(states);
    const paneInfo = useSelector(pane);
    dispatch = useDispatch();
    gr.groups = {
        0: {
            nam: "Расписание звонков",
            linke: "zvonki"
        },
        1: {
            nam: (cState.auth && cState.role < 2) ? "Расписание периодов" : "Периоды обучения",
            linke: "periods"
        },
        2: {
            nam: (cState.auth && cState.role < 2) ? "Расписание" : "Дисциплины",
            linke: "schedule"
        },
        3: cState.auth && cState.role < 2 ? {
            nam: "Журнал",
            linke: "journal"
        } : undefined,
        4: cState.auth && cState.role < 2 ? {
            nam: "Итоговые оценки",
            linke: "marks"
        } : undefined
    };
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount AnalyticsMain.jsx");
        setActived(cState.role == 3 ? 10 : 13);
        return function() {
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount AnalyticsMain.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate AnalyticsMain.jsx');
    });
    return (
        <div className={analyticsCSS.AppHeader}>
            <div style={{width:"inherit", height: "7vh", position: "fixed", zIndex:"1"}} ref={()=>(ke = !ke ? paneInfo.els.length : ke)}>
                <Pane gro={gr}/>
            </div>
            <Outlet />
            {props.comp && props.comp}
        </div>
    )
}
export default AnalyticsMain;