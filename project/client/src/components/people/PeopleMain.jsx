import React, {useEffect, useRef} from "react";
import peopleCSS from './peopleMain.module.css';
import {Outlet} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {groups, states} from "../../store/selector";
import Pane from "../other/pane/Pane";
import {eventSource, send, setActived} from "../main/Main";
import {
    CHANGE_EVENT,
    CHANGE_GROUPS_DEL_GRS,
    CHANGE_GROUPS_GL,
    CHANGE_GROUPS_GR,
    CHANGE_GROUPS_GRS,
    CHANGE_PARENTS,
    CHANGE_PARENTS_DEL,
    CHANGE_PARENTS_DEL_L0,
    CHANGE_TEACHERS,
    changeEvents,
    changeGroups,
    changePeople
} from "../../store/actions";
import parentsCSS from "./parents/parents.module.css";
import {addKid, codPar} from "./parents/Parents";
import {addTea, codTea} from "./teachers/Teachers";

let gr, cState, dispatch, groupsInfo;
gr = {
    group: 0
};

export let sit = "http://localhost:3000";

export function copyLink(e, link, name) {
    let title, text;
    title = "Внимание!";
    text = "Ссылка-приглашение для " + name + " успешно скопирована в буфер обмена.";
    navigator.clipboard.writeText(link);
    dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
}

export function gen_cod(){
    var password = "";
    var symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < 15; i++){
        password += symbols.charAt(Math.floor(Math.random() * symbols.length));
    }
    return password;
}

export function refreshLink(e, sit, type) {
    let inp, id, title, text;
    title = "Внимание!";
    text = "Ссылка успешно обновлена"
    inp = e.target.parentElement.querySelector("input");
    if (inp.hasAttribute("data-id")) {
        id = inp.getAttribute("data-id").split("_");
        if(type == CHANGE_PARENTS){
            codPar(id[0], id[1], title, text);
            // dispatch(changePeople(type, id[0], "par", id[1], sit + "/invite/" + gen_cod(), "link"));
        } else {
            codTea(id[0], id[1], title, text);
            // dispatch(changePeople(type, id[0], id[1], undefined, sit + "/invite/" + gen_cod(), "link"));
        }
        // dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
    } else if (inp.hasAttribute("data-id1")) {
        id = inp.getAttribute("data-id1");
        dispatch(changePeople(type, 2, id, undefined, sit + "/invite/" + gen_cod(), "link"));
        dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
    }
}

export function onDel(e, type, info) {
    let par, inp, id;
    par = e.target.parentElement.parentElement;
    if(par.classList.contains(peopleCSS.pepl)){
        inp = par.querySelector("input:not([readOnly])");
        if (inp.hasAttribute("data-id")) {
            id = inp.getAttribute("data-id").split("_");
            if(type == CHANGE_PARENTS_DEL) {
                if(Object.getOwnPropertyNames(info[id[0]].par).length < 2){
                    dispatch(changePeople(CHANGE_PARENTS_DEL_L0, id[0]));
                } else {
                    dispatch(changePeople(type, id[0], "par", id[1]));
                }
            } else {
                dispatch(changePeople(type, id[0], id[1]));
            }
        } else if(inp.hasAttribute("data-id1")){
            let id = inp.getAttribute("data-id1");
            if(type == CHANGE_PARENTS_DEL) {
                dispatch(changePeople(type, "nw", "par", id));
            } else {
                dispatch(changePeople(type, 2, id));
            }
        }
    } else if(par.classList.contains(peopleCSS.nav_iZag)){
        if(e.target.hasAttribute("data-id1")){
            let id = e.target.getAttribute("data-id1");
            if(type == CHANGE_PARENTS_DEL_L0) {
                dispatch(changePeople(type, id));
            }
        }
    }
}

export function onEdit(e) {
    let par = e.target.parentElement;
    if(par.classList.contains(peopleCSS.add)){
        par.setAttribute('data-st', '1');
    }
    if(par.parentElement.classList.contains(peopleCSS.pepl)){
        par = par.parentElement;
        par.setAttribute('data-st', '1');
    }
}

export function onFin(e, inps, forceUpdate, type, info) {
    let par, inp;
    par = e.target.parentElement;
    if (par.classList.contains(parentsCSS.upr)) {
        par = par.parentElement;
        addKid({...info.nw}, inps.nyid, par);
        // dispatch(changePeople(CHANGE_PARENTS_L1, undefined, inps.nyid, undefined, {...info.nw}));
        // inps.nyid = undefined;
        // dispatch(changePeople(CHANGE_PARENTS_DEL_L1, "nw", "par"));
        par.setAttribute('data-st', '0');
        return;
    }
    if (par.classList.contains(peopleCSS.fi)){
        par = par.parentElement;
        let grop, id, inp;
        if(type == CHANGE_PARENTS) {
            inp = par.querySelector("input");
            par = par.parentElement;
            if(inp.hasAttribute("data-id1")) {
                id = inp.getAttribute("data-id1");
                grop = info[id] && info[id].par ? Object.getOwnPropertyNames(info[id].par) : [];
                let id1 = grop.length == 0 ? "id0" : "id" + (parseInt(grop[grop.length-1].replace("id", "")) + 1);
                dispatch(changePeople(type, id, "par", id1, inps.inpnpt));
            } else {
                grop = info.nw && info.nw.par ? Object.getOwnPropertyNames(info.nw.par) : [];
                id = grop.length == 0 ? "id0" : "id" + (parseInt(grop[grop.length-1].replace("id", "")) + 1);
                dispatch(changePeople(type, "nw", "par", id, inps.inpnpt));
            }
            par.setAttribute('data-st', '0');
        } else if(type == CHANGE_TEACHERS) {
            par = par.parentElement;
            addTea(inps.inpnpt, par);
            // dispatch(changePeople(type, "nt", "id8", undefined, inps.inpnpt));
        } else {
            par = par.parentElement;
            dispatch(changePeople(type, 2, "id8", undefined, inps.inpnpt));
            par.setAttribute('data-st', '0');
        }
        return;
    }
    inp = par.querySelector("input");
    if (inps[inp.id]) {
        inp.setAttribute("data-mod", '0');
        if(par.parentElement.classList.contains(peopleCSS.pepl)) {
            par = par.parentElement;
            if(type){
                if(inp.hasAttribute("data-id")){
                    let id = inp.getAttribute("data-id").split("_");
                    if(type == CHANGE_PARENTS) {
                        dispatch(changePeople(type, id[0], "par", id[1], inp.value));
                    } else {
                        dispatch(changePeople(type, id[0], id[1], undefined, inp.value));
                    }
                } else if(inp.hasAttribute("data-id1")){
                    let id = inp.getAttribute("data-id1");
                    if(type == CHANGE_PARENTS) {
                        dispatch(changePeople(type, "nw", "par", id, inp.value));
                    } else {
                        dispatch(changePeople(type, 2, id, undefined, inp.value));
                    }
                }
            } else {
                inps.inpnpt = inp.value;
                forceUpdate();
            }
        }
        par.setAttribute('data-st', '0');
    } else {
        inp.setAttribute("data-mod", '1');
    }
}

export function onClose(e, type) {
    let par = e.target.parentElement;
    if(par.parentElement.classList.contains(peopleCSS.pepl)){
        if(par.classList.contains(peopleCSS.fi) || type) {
            par = par.parentElement.parentElement;
        } else {
            par = par.parentElement;
        }
        par.setAttribute('data-st', '0');
    }
}

export function chStatB(e, inps) {
    let el = e.target;
    inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
    if (inps[el.id]) {
        el.setAttribute("data-mod", '0');
    } else {
        el.setAttribute("data-mod", '1');
    }
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +inps[el.id]);
}

export function ele (x, par, inps) {
    if(!inps[par]) inps[par] = x;
}

function remGroupC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeGroups(CHANGE_GROUPS_DEL_GRS, undefined, undefined, msg.id));
}

function chGroupC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeGroups(CHANGE_GROUPS_GRS, undefined, msg.name, msg.id));
}

function addGroupC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeGroups(CHANGE_GROUPS_GRS, undefined, msg.name, msg.id));
}

export function remGroup (id) {
    console.log("remGroup");
    send({
        uuid: cState.uuid,
        id: id
    }, 'POST', "hteachers", "remGroup")
}

export function chGroup (id, inp, par) {
    console.log("chGroup");
    send({
        uuid: cState.uuid,
        id: id,
        name: inp
    }, 'POST', "hteachers", "chGroup")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

export function addGroup (inp, par) {
    console.log("addGroup");
    send({
        uuid: cState.uuid,
        name: inp
    }, 'POST', "hteachers", "addGroup")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

function onCon(e) {
    setGroups();
}

function setGroups() {
    send({
        uuid: cState.uuid
    }, 'POST', "hteachers", "getGroups")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changeGroups(CHANGE_GROUPS_GL, undefined, data.body));
                if(!data.body[groupsInfo.group]){
                    let grs = Object.getOwnPropertyNames(data.body);
                    dispatch(changeGroups(CHANGE_GROUPS_GR, undefined, parseInt(grs[0])));
                }
            }
        });
}

export function setActNew(name) {
    gr.group = name;
}

export function PeopleMain() {
    cState = useSelector(states);
    groupsInfo = useSelector(groups);
    if(!dispatch && cState.role > 1){
        if(eventSource.readyState == EventSource.OPEN) setGroups();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addGroupC', addGroupC, false);
        eventSource.addEventListener('chGroupC', chGroupC, false);
        eventSource.addEventListener('remGroupC', remGroupC, false);
    }
    dispatch = useDispatch();
    gr.groups = {
        0: cState.auth && (cState.role < 2 || cState.role == 3) ? {
            nam: "Педагоги",
            linke: "teachers"
        } : undefined,
        1: cState.auth ? {
            nam: "Завучи",
            linke: "hteachers"
        } : undefined,
        2: cState.auth && (cState.role == 0 || cState.role == 3) ? {
            nam: cState.role == 3 ? "Обучающиеся" : "Одноклассники",
            linke: "class"
        } : undefined,
        3: cState.auth && (cState.role == 0 || cState.role == 3) ? {
            nam: "Родители",
            linke: "parents"
        } : undefined,
        4: {
            nam: "Администраторы портала",
            linke: "admins"
        }
    };
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount PeopleMain.jsx");
        setActived(3);
        return function() {
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addGroupC', addGroupC);
            eventSource.removeEventListener('chGroupC', chGroupC);
            eventSource.removeEventListener('remGroupC', remGroupC);
            console.log("I was triggered during componentWillUnmount PeopleMain.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate PeopleMain.jsx');
    });
    return (
        <div className={peopleCSS.AppHeader}>
            <div style={{width:"inherit", height: "7vh", position: "fixed", zIndex:"1"}}>
                <Pane gro={gr}/>
            </div>
            <Outlet />
        </div>
    )
}
export default PeopleMain;