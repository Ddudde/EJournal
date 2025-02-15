import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import {classmates, groups, states, themes} from "../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {ele, goToProf, setActNew, setEvGr, sit} from "./PeopleMain";
import profl from "../../media/profl.png";
import profd from "../../media/profd.png";
import Pane from "../other/pane/Pane";
import ErrFound from "../other/error/ErrFound";
import peopleCSS from "./peopleMain.module.css";
import {
    CHANGE_CLASSMATES,
    CHANGE_CLASSMATES_DEL,
    CHANGE_CLASSMATES_EL_GL,
    CHANGE_CLASSMATES_GL,
    CHANGE_EVENT,
    CHANGE_EVENTS_CLEAR,
    CHANGE_GROUPS_GL,
    CHANGE_GROUPS_GR,
    changeEvents,
    changeGroups,
    changePeople
} from "../../store/actions";
import ed from "../../media/edit.png";
import no from "../../media/no.png";
import refreshCd from "../../media/refreshCd.png";
import refreshCl from "../../media/refreshCl.png";
import copyd from "../../media/copyd.png";
import copyl from "../../media/copyl.png";
import yes from "../../media/yes.png";
import {eventSource, sendToServer} from "../main/Main";
import {cAuth, cStudents} from "../other/Controllers";

let dispatch, classmatesInfo, groupsInfo, selGr, errText, inps, themeState, cState, tps;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
inps = {inpnpt : "Фамилия И.О."};
selGr = 0;
tps = {
    del : CHANGE_CLASSMATES_DEL,
    ch: CHANGE_CLASSMATES,
    el_gl: CHANGE_CLASSMATES_EL_GL,
    gl: CHANGE_CLASSMATES_GL
};
let [_, forceUpdate] = [];

function copyLink(e, link, name) {
    let title, text;
    title = "Внимание!";
    text = "Ссылка-приглашение для " + name + " успешно скопирована в буфер обмена.";
    navigator.clipboard.writeText(link);
    dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
}

function refreshLink(e) {
    let inp, id, title, text;
    title = "Внимание!";
    text = "Ссылка успешно обновлена"
    inp = e.target.parentElement.querySelector("input");
    if (inp.hasAttribute("data-id")) {
        id = inp.getAttribute("data-id").split("_");
        sendToServer({
            id: id[1]
        }, 'PATCH', cAuth+"setCodePep")
            .then(data => {
                console.log(data);
                if(data.status == 200){
                    dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
                }
            });
    }
}

function onDel(e, type) {
    let par, inp, id;
    par = e.target.parentElement.parentElement;
    if(par.classList.contains(peopleCSS.pepl)){
        inp = par.querySelector("input:not([readOnly])");
        if (inp.hasAttribute("data-id")) {
            id = inp.getAttribute("data-id").split("_");
            remInv(type, id[1]);
        }
    }
}

function onEdit(e) {
    let par;
    par = e.target.parentElement;
    if(par.classList.contains(peopleCSS.add)){
        par.setAttribute('data-st', '1');
    }
    if(par.parentElement.classList.contains(peopleCSS.pepl)){
        par = par.parentElement;
        par.setAttribute('data-st', '1');
    }
}

function onFin(e, type) {
    let par, inp;
    par = e.target.parentElement;
    if (par.classList.contains(peopleCSS.fi)){
        par = par.parentElement.parentElement;
        addInv(type, inps.inpnpt, par);
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
                    changeInv(type, id[1], inp.value, par);
                }
            } else {
                inps.inpnpt = inp.value;
                forceUpdate();
                par.setAttribute('data-st', '0');
            }
        }
    } else {
        inp.setAttribute("data-mod", '1');
    }
}

function onClose(e) {
    let par = e.target.parentElement;
    if(par.parentElement.classList.contains(peopleCSS.pepl)){
        if(par.classList.contains(peopleCSS.fi)) {
            par = par.parentElement.parentElement;
        } else {
            par = par.parentElement;
        }
        par.setAttribute('data-st', '0');
    }
}

function chStatB(e) {
    let el = e.target;
    inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
    if (inps[el.id]) {
        el.setAttribute("data-mod", '0');
    } else {
        el.setAttribute("data-mod", '1');
    }
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +inps[el.id]);
}

function codPepC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ch, 0, msg.id, undefined, msg.code, "link"));
}

function remPepC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.del, 0, msg.id));
}

function chPepC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ch, 0, msg.id, undefined, msg.name));
}

function addPepC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.el_gl, 0, msg.id, undefined, msg.body));
}

function remInv (type, id) {
    console.log("remInv");
    sendToServer({
        id: id
    }, 'DELETE', cStudents+"remPep")
}

function changeInv (type, id, inp, par) {
    console.log("changeInv");
    sendToServer({
        id: id,
        name: inp
    }, 'PATCH', cStudents+"chPep")
        .then(data => {
            console.log(data);
            if(data.status == 200){
                par.setAttribute('data-st', '0');
            }
        });
}

function addInv (type, inp, par) {
    console.log("addInv");
    sendToServer({
        name: inp
    }, 'POST', cStudents+"addPep")
        .then(data => {
            console.log(data);
            if(data.status == 201){
                par.setAttribute('data-st', '0');
            }
        });
}

function onCon(e) {
    setInfo();
}

function setStud(firstG, bodyG) {
    selGr = firstG != undefined ? firstG : groupsInfo.els.group;
    sendToServer(0, 'GET', cStudents+"getStud/"+selGr)
        .then(data => {
            console.log(data);
            dispatch(changePeople(tps.gl, undefined, undefined, undefined, data.body));
            if(firstG != undefined) {
                dispatch(changeGroups(CHANGE_GROUPS_GL, undefined, bodyG));
                dispatch(changeGroups(CHANGE_GROUPS_GR, undefined, firstG));
            }
            if(data.status == 200){
                for(let el of document.querySelectorAll("." + peopleCSS.ed + " > *[id^='inpn']")){
                    chStatB({target: el});
                }
            }
        });
}

function setInfo() {
    var url = "getInfo";
    if(cState.role == 3) url = "getInfoFH";
    sendToServer(0, 'GET', cStudents + url)
        .then(data => {
            console.log(data);
            if(data.status == 200){
                if(cState.role == 3) {
                    setEvGr(cState, dispatch);
                    setStud(parseInt(data.body.firstG), data.body.bodyG);
                } else {
                    setStud();
                }
            }
        });
}

function getBlock(x, b) {
    let edFi, info, codeLink;
    info = classmatesInfo[x];
    if(info && info.link) {
        codeLink = sit + (info.login ? "/reauth/" : "/invite/") + info.link;
    }
    edFi = <div className={peopleCSS.pepl} key={x} data-st="0">
        {x ?
            <div className={peopleCSS.fi}>
                <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                    {info.name}
                </div>
                {info.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info.login)} title="Перейти в профиль" alt=""/>}
                <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={e=>onDel(e, tps.del)} title="Удалить" alt=""/>
                <input className={peopleCSS.inp+" "+peopleCSS.copyInp} data-id={x ? info.login+"_"+x : undefined} id={"inpcpt_" + x} placeholder="Ссылка не создана" defaultValue={codeLink} type="text" readOnly/>
                <img className={peopleCSS.imginp+" "+peopleCSS.refrC} src={themeState.theme_ch ? refreshCd : refreshCl} onClick={refreshLink} title="Создать ссылку-приглашение" alt=""/>
                <img className={peopleCSS.imginp} src={themeState.theme_ch ? copyd : copyl} title="Копировать" data-enable={info.link ? "1" : "0"} onClick={(e)=>copyLink(e, codeLink, info.name)} alt=""/>
            </div>
            :
            <div className={peopleCSS.fi}>
                <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                    {inps.inpnpt}
                </div>
                <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} title="Так будет выглядеть иконка перехода в профиль" alt=""/>
                <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                <img className={peopleCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, tps.ch)} title="Подтвердить" alt=""/>
                <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
            </div>
        }
        <div className={peopleCSS.ed}>
            <div className={peopleCSS.preinf}>
                ФИО:
            </div>
            <input className={peopleCSS.inp} data-id={x ? info.login+"_"+x : undefined} id={"inpnpt_" + (x?x:"")} placeholder={"Фамилия И.О."} defaultValue={x ? info.name : inps.inpnpt} onChange={chStatB} type="text"/>
            {ele(false, "inpnpt_" + (x?x:""), inps)}
            <img className={peopleCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, x ? tps.ch : undefined)} title="Подтвердить" alt=""/>
            <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
        </div>
    </div>;
    return b ? edFi :
        <div className={peopleCSS.add+" "+peopleCSS.nav_iZag} data-st="0">
            <div className={peopleCSS.nav_i+" "+peopleCSS.link} id={peopleCSS.nav_i} onClick={onEdit}>
                Добавить ученика
            </div>
            {edFi}
        </div>
}

export function Classmates() {
    classmatesInfo = useSelector(classmates);
    cState = useSelector(states);
    themeState = useSelector(themes);
    groupsInfo = useSelector(groups);
    if(!dispatch) {
        setActNew(2);
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addPepC', addPepC, false);
        eventSource.addEventListener('chPepC', chPepC, false);
        eventSource.addEventListener('remPepC', remPepC, false);
        eventSource.addEventListener('codPepL1C', codPepC, false);
    }
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Classmates.jsx");
        for(let el of document.querySelectorAll("." + peopleCSS.ed + " > *[id^='inpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addPepC', addPepC);
            eventSource.removeEventListener('chPepC', chPepC);
            eventSource.removeEventListener('remPepC', remPepC);
            eventSource.removeEventListener('codPepL1C', codPepC);
            console.log("I was triggered during componentWillUnmount Classmates.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(groupsInfo.els.group && selGr != groupsInfo.els.group && eventSource.readyState == EventSource.OPEN){
            setStud();
        }
        console.log('componentDidUpdate Classmates.jsx');
    });
    return (
        <div className={peopleCSS.header}>
            <Helmet>
                <title>{cState.role == 3 ? "Обучающиеся" : "Одноклассники"}</title>
            </Helmet>
            {Object.getOwnPropertyNames(classmatesInfo).length == 0 && !(cState.auth && cState.role == 3) ?
                    <ErrFound text={errText}/>
                :
                    <>
                        {(cState.auth && cState.role == 3) &&
                            <div style={{width:"inherit", height: "7vh", position: "fixed", zIndex:"1"}}>
                                <Pane cla={true}/>
                            </div>
                        }
                        <div className={peopleCSS.blockPep}>
                            <div className={peopleCSS.pep}>
                                <div className={peopleCSS.nav_iZag}>
                                    <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                                        {cState.role == 3 ? "Обучающиеся" : "Одноклассники"}
                                    </div>
                                    {cState.auth && cState.role == 3 ? <>
                                            {getBlock()}
                                            {Object.getOwnPropertyNames(classmatesInfo).map(param =>
                                                getBlock(param, true)
                                            )}
                                        </> :
                                        Object.getOwnPropertyNames(classmatesInfo).map(param =>
                                            <div key={param}>
                                                <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                                    {classmatesInfo[param].name}
                                                </div>
                                                {classmatesInfo[param].login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(classmatesInfo[param].login)} title="Перейти в профиль" alt=""/>}
                                            </div>
                                        )
                                    }
                                </div>
                            </div>
                        </div>
                    </>
            }
        </div>
    )
}
export default Classmates;