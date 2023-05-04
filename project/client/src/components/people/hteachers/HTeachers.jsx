import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import {useNavigate} from "react-router-dom";
import peopleCSS from '../peopleMain.module.css';
import {hteachers, states, themes} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {ele, setActNew, sit} from "../PeopleMain";
import profl from "../../../media/profl.png";
import profd from "../../../media/profd.png";
import ErrFound from "../../other/error/ErrFound";
import ed from "../../../media/edit.png";
import yes from "../../../media/yes.png";
import no from "../../../media/no.png";
import {
    CHANGE_EVENT,
    CHANGE_EVENTS_CLEAR,
    CHANGE_HTEACHERS,
    CHANGE_HTEACHERS_DEL,
    CHANGE_HTEACHERS_DEL_L2,
    CHANGE_HTEACHERS_EL_GL,
    CHANGE_HTEACHERS_GL,
    CHANGE_HTEACHERS_L2,
    CHANGE_HTEACHERS_L2_GL,
    changeEvents,
    changePeople
} from "../../../store/actions";
import refreshCd from "../../../media/refreshCd.png";
import refreshCl from "../../../media/refreshCl.png";
import copyd from "../../../media/copyd.png";
import copyl from "../../../media/copyl.png";
import {eventSource, send} from "../../main/Main";

let dispatch, errText, cState, inps, navigate, hteachersInfo, themeState, tps;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
inps = {inpnpt : "Поле для ввода"};
tps = {
    "ht" : {
        del : CHANGE_HTEACHERS_DEL,
        ch: CHANGE_HTEACHERS,
        el_gl: CHANGE_HTEACHERS_EL_GL
    },
    "ht4" : {
        del : CHANGE_HTEACHERS_DEL,
        ch: CHANGE_HTEACHERS,
        el_gl: CHANGE_HTEACHERS_EL_GL
    },
    "ht4L2" : {
        del : CHANGE_HTEACHERS_DEL_L2,
        ch: CHANGE_HTEACHERS_L2,
        el_gl: CHANGE_HTEACHERS_L2_GL
    }
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
    text = "Ссылка успешно обновлена";
    inp = e.target.parentElement.parentElement.querySelector("input:not([readOnly])");
    if (inp.hasAttribute("data-id")) {
        id = inp.getAttribute("data-id").split("_");
        send({
            uuid: cState.uuid,
            id: id[0],
            id1: id[1]
        }, 'POST', "auth/setCodePep")
            .then(data => {
                console.log(data);
                if(data.error == false){
                    dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
                }
            });
    } else if (inp.hasAttribute("data-id1")) {
        id = inp.getAttribute("data-id1").split("_");
        // dispatch(changePeople(type, 2, id, undefined, sit + (id[0] ? "/reauth/" : "/invite/") + gen_cod(), "link"));
        // dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
        send({
            uuid: cState.uuid,
            id: id[1],
            id1: id[0]
        }, 'POST', "auth/setCodePep")
            .then(data => {
                console.log(data);
                if(data.error == false){
                    dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
                }
            });
    }
}

function onDel(e) {
    let par, inp, id;
    par = e.target.parentElement.parentElement;
    if(par.classList.contains(peopleCSS.pepl)){
        inp = par.querySelector("input:not([readOnly])");
        if (inp.hasAttribute("data-id")) {
            id = inp.getAttribute("data-id").split("_");
            // dispatch(changePeople(type, 0, id[0], id[1]));
            remPep(id[0], id[1]);
        } else if(inp.hasAttribute("data-id1")){
            let id = inp.getAttribute("data-id1").split("_");
            // dispatch(changePeople(type, 2, id));
            if(cState.role == 4) {
                remSch(id[0]);
            } else if(cState.role == 3) {
                remPep(id[1], id[0]);
            }
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
        par = par.parentElement;
        if(type == CHANGE_HTEACHERS_L2){
            par = par.parentElement;
            if(e.target.hasAttribute("data-id1")){
                let id = e.target.getAttribute("data-id1");
                addPep(par, id, inps.inpnpt);
                // dispatch(changePeople(type, 2, id, "id8", inps.inpnpt));
            }
        } else {
            par = par.parentElement;
            if(cState.role == 4) {
                addSch(par, inps.inpnpt);
            } else if(cState.role == 3) {
                addPep(par, undefined, inps.inpnpt);
            }
        }
        // par.setAttribute('data-st', '0');
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
                    chPep(par, id[0], id[1], inp.value);
                    // dispatch(changePeople(type, 0, id[0], id[1], inp.value));
                } else if(inp.hasAttribute("data-id1")){
                    let id = inp.getAttribute("data-id1").split("_");
                    if(cState.role == 4) {
                        chSch(par, id[0], inp.value);
                    } else if(cState.role == 3) {
                        chPep(par, id[1], id[0], inp.value);
                    }
                }
            } else {
                inps.inpnpt = inp.value;
                forceUpdate();
                par.setAttribute('data-st', '0');
            }
        }
        // par.setAttribute('data-st', '0');
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

function goToProf(log) {
    if(log) navigate("/profiles/" + log);
}

function onCon(e) {
    setInfo();
}

function codPepL2C(e) {
    console.log(e.data);
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4L2.ch, 0, msg.id1, msg.id, msg.code, "link"));
}

function codPepL1C(e) {
    console.log(e.data);
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4.ch, 0, msg.id, undefined, msg.code, "link"));
}

function remInfoL1C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4.del, 0, msg.id));
}

function chInfoL1C(e) {
    const msg = JSON.parse(e.data);
    console.log(msg);
    dispatch(changePeople(tps.ht4.ch, 0, msg.id, undefined, msg.name));
}

function addInfoL1C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4.el_gl, 0, msg.id, undefined, msg.body));
}

function remInfoL2C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4L2.del, 0, msg.id1, msg.id));
}

function chInfoL2C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4L2.ch, 0, msg.id1, msg.id, msg.name));
}

function addInfoL2C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(tps.ht4L2.el_gl, 0, msg.id1, msg.id, msg.body));
}

function addSch (par, inp) {
    console.log("addSch");
    send({
        uuid: cState.uuid,
        name: inp
    }, 'POST', "hteachers", "addSch")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

function remSch (id) {
    console.log("remSch");
    send({
        uuid: cState.uuid,
        id: id
    }, 'POST', "hteachers", "remSch")
}

function chSch (par, id, inp) {
    console.log("chSch");
    send({
        uuid: cState.uuid,
        id: id,
        name: inp
    }, 'POST', "hteachers", "chSch")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

function remPep (id, id1) {
    console.log("remInv");
    send({
        uuid: cState.uuid,
        id: id,
        id1: id1,
        role: cState.role
    }, 'POST', "hteachers", "remPep")
}

function chPep (par, id, id1, inp) {
    console.log("changeInv");
    send({
        uuid: cState.uuid,
        id: id,
        id1: id1,
        name: inp,
        role: cState.role
    }, 'POST', "hteachers", "chPep")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

function addPep (par, id, inp) {
    console.log("addInv");
    send({
        uuid: cState.uuid,
        yo: id,
        name: inp,
        role: cState.role
    }, 'POST', "hteachers", "addPep")
        .then(data => {
            console.log(data);
            if(data.error == false){
                par.setAttribute('data-st', '0');
            }
        });
}

function setInfo() {
    send({
        role: cState.role,
        uuid: cState.uuid
    }, 'POST', "hteachers", "getInfo")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changePeople(CHANGE_HTEACHERS_GL, undefined, undefined, undefined, data.body));
            }
        });
    for(let el of document.querySelectorAll("." + peopleCSS.ed + " > *[id^='inpn']")){
        chStatB({target: el});
    }
}

function getBlock(title, typ, x, b, info, x1) {
    let edFi;
    edFi = <div className={peopleCSS.pepl} style={{marginLeft: typ == "ht4L2" ? "2vw" : undefined}} key={x1 ? x1 : x} data-st="0">
        {b ?
            <div className={peopleCSS.fi}>
                <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                    {info.name}
                </div>
                {(typ != "ht4" && info.login) && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info.login)} title="Перейти в профиль" alt=""/>}
                <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onDel} title="Удалить" alt=""/>
                {typ != "ht4" && <>
                    <input className={peopleCSS.inp+" "+peopleCSS.copyInp} id={"inpcpt_" + x} placeholder="Ссылка не создана" defaultValue={info.link ? sit + (info.login ? "/reauth/" : "/invite/") + info.link : undefined} type="text" readOnly/>
                    <img className={peopleCSS.imginp+" "+peopleCSS.refrC} src={themeState.theme_ch ? refreshCd : refreshCl} onClick={refreshLink} title="Создать ссылку-приглашение" alt=""/>
                    <img className={peopleCSS.imginp} src={themeState.theme_ch ? copyd : copyl} title="Копировать" data-enable={info.link ? "1" : "0"} onClick={(e)=>copyLink(e, info.link ? sit + (info.login ? "/reauth/" : "/invite/") + info.link : undefined, info.name)} alt=""/>
                </>}
            </div>
            :
            <div className={peopleCSS.fi}>
                <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                    {inps.inpnpt}
                </div>
                {typ != "ht4" && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} title="Так будет выглядеть иконка перехода в профиль" alt=""/>}
                <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                <img className={peopleCSS.imginp+" yes "} data-id1={typ == "ht4L2" ? x : undefined} src={yes} onClick={e=>onFin(e, tps[typ].ch)} title="Подтвердить" alt=""/>
                <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
            </div>
        }
        <div className={peopleCSS.ed}>
            <div className={peopleCSS.preinf}>
                ФИО:
            </div>
            <input className={peopleCSS.inp} data-id={x1 ? info.login+"_"+x1 : undefined} data-id1={info ? x+"_"+info.login : x+"_"} id={"inpnpt_" + (x?x:"")} placeholder={"Фамилия И.О."} defaultValue={b ? info.name : inps.inpnpt} onChange={chStatB} type="text"/>
            {ele(false, "inpnpt_" + (x?x:""), inps)}
            <img className={peopleCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, b ? tps[typ].ch : undefined)} title="Подтвердить" alt=""/>
            <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
        </div>
    </div>;
    return b ? edFi :
        <div className={peopleCSS.add+" "+peopleCSS.nav_iZag} style={{marginLeft: typ == "ht4L2" ? "2vw" : undefined}} data-st="0">
            <div className={peopleCSS.nav_i+" "+peopleCSS.link} id={peopleCSS.nav_i} onClick={onEdit}>
                {title}
            </div>
            {edFi}
        </div>
}

export function HTeachers() {
    hteachersInfo = useSelector(hteachers);
    themeState = useSelector(themes);
    navigate = useNavigate();
    cState = useSelector(states);
    if(!dispatch) {
        setActNew(1);
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addInfoL1C', addInfoL1C, false);
        eventSource.addEventListener('chInfoL1C', chInfoL1C, false);
        eventSource.addEventListener('remInfoL1C', remInfoL1C, false);
        eventSource.addEventListener('addInfoL2C', addInfoL2C, false);
        eventSource.addEventListener('remInfoL2C', remInfoL2C, false);
        eventSource.addEventListener('chInfoL2C', chInfoL2C, false);
        eventSource.addEventListener('codPepL1C', codPepL1C, false);
        eventSource.addEventListener('codPepL2C', codPepL2C, false);
    }
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount HTeachers.jsx");
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addInfoL1C', addInfoL1C);
            eventSource.removeEventListener('chInfoL1C', chInfoL1C);
            eventSource.removeEventListener('remInfoL1C', remInfoL1C);
            eventSource.removeEventListener('addInfoL2C', addInfoL2C);
            eventSource.removeEventListener('remInfoL2C', remInfoL2C);
            eventSource.removeEventListener('chInfoL2C', chInfoL2C);
            eventSource.removeEventListener('codPepL1C', codPepL1C);
            eventSource.removeEventListener('codPepL2C', codPepL2C);
            console.log("I was triggered during componentWillUnmount HTeachers.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate HTeachers.jsx');
    });
    return (
        <div className={peopleCSS.header}>
            <Helmet>
                <title>{cState.role == 4 ? "Администрации учебных организаций" : "Администрация учебной организации"}</title>
            </Helmet>
            {Object.getOwnPropertyNames(hteachersInfo).length == 0 && !(cState.auth && cState.role > 2) ?
                <ErrFound text={errText}/>
                :
                <div className={peopleCSS.blockPep}>
                    <div className={peopleCSS.pep}>
                        <div className={peopleCSS.nav_iZag}>
                            <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                                {cState.role == 4 ? "Администрации учебных организаций" : "Администрация учебной организации"}
                            </div>
                            {cState.auth && cState.role > 2 ? cState.role == 3 ? <>
                                        {getBlock("Добавить завуча", "ht")}
                                        {Object.getOwnPropertyNames(hteachersInfo).map(param =>
                                            getBlock(undefined, "ht", param, true, hteachersInfo[param])
                                        )}
                                    </> : <>
                                        {getBlock("Добавить учебную организацию", "ht4")}
                                        {Object.getOwnPropertyNames(hteachersInfo).map(param =><>
                                            {getBlock(undefined, "ht4", param, true, hteachersInfo[param])}
                                            {getBlock("Добавить завуча", "ht4L2", param)}
                                            {hteachersInfo[param].pep && Object.getOwnPropertyNames(hteachersInfo[param].pep).map(param1 =>
                                                getBlock(undefined, "ht4L2", param, true, hteachersInfo[param].pep[param1], param1)
                                            )}
                                        </>)}
                                </> :
                                Object.getOwnPropertyNames(hteachersInfo).map(param =>
                                    <div key={param}>
                                        <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                            {hteachersInfo[param].name}
                                        </div>
                                        {hteachersInfo[param].login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(hteachersInfo[param].login)} title="Перейти в профиль" alt=""/>}
                                    </div>
                                )
                            }
                        </div>
                    </div>
                </div>
            }
        </div>
    )
}
export default HTeachers;