import React, {useEffect, useRef} from "react";
import peopleCSS from './peopleMain.module.css';
import {Outlet, useNavigate} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {groups, states} from "../../store/selector";
import Pane from "../other/pane/Pane";
import {eventSource, prefSite, sendToServer, setActived} from "../main/Main";
import {
    CHANGE_EVENT,
    CHANGE_GROUPS_DEL_GRS,
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
import {addTea, codTea} from "./Teachers";
import {cHteachers} from "../other/Controllers";

let gr, cState, dispatch, groupsInfo, evsIni, navigate;
gr = {
    group: 0
};

export let sit = window.location.origin;

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
            codPar(id[1], title, text);
        } else {
            codTea(id[1], title, text);
        }
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

export function goToProf(log) {
    if(log) navigate(prefSite + "/profiles/" + log);
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

export function setActNew(name) {
    gr.group = name;
}

export function remGroup (id) {
    console.log("remGroup");
    sendToServer({
        uuid: cState.uuid,
        grId: id
    }, 'POST', cHteachers+"remGroup")
}

export function chGroup (id, inp, par) {
    console.log("chGroup");
    sendToServer({
        uuid: cState.uuid,
        grId: id,
        name: inp
    }, 'POST', cHteachers+"chGroup")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

export function addGroup (inp, par) {
    console.log("addGroup");
    sendToServer({
        uuid: cState.uuid,
        name: inp
    }, 'POST', cHteachers+"addGroup")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

export function setEvGr(cS, dis) {
    if(cS) cState = cS;
    if(dis) dispatch = dis;
    if(!evsIni) {
        evsIni = true;
        eventSource.addEventListener('addGroupC', addGroupC, false);
        eventSource.addEventListener('chGroupC', chGroupC, false);
        eventSource.addEventListener('remGroupC', remGroupC, false);
    }
}

export function PeopleMain() {
    cState = useSelector(states);
    groupsInfo = useSelector(groups);
    navigate = useNavigate();
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
            eventSource.removeEventListener('addGroupC', addGroupC);
            eventSource.removeEventListener('chGroupC', chGroupC);
            eventSource.removeEventListener('remGroupC', remGroupC);
            evsIni = false;
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