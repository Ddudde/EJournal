import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import peopleCSS from "../peopleMain.module.css";
import {states, teachers, themes} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {chStatB, copyLink, ele, onClose, onDel, onEdit, onFin, refreshLink, setActNew, sit} from "../PeopleMain";
import profl from "../../../media/profl.png";
import profd from "../../../media/profd.png";
import {useNavigate} from "react-router-dom";
import copyl from "../../../media/copyl.png";
import copyd from "../../../media/copyd.png";
import refreshCd from "../../../media/refreshCd.png";
import refreshCl from "../../../media/refreshCl.png";
import {
    CHANGE_EVENT,
    CHANGE_EVENTS_CLEAR,
    CHANGE_TEACHERS,
    CHANGE_TEACHERS_DEL,
    CHANGE_TEACHERS_GL,
    changeEvents,
    changePeople
} from "../../../store/actions";
import ed from "../../../media/edit.png";
import yes from "../../../media/yes.png";
import no from "../../../media/no.png";
import ErrFound from "../../other/error/ErrFound";
import {eventSource, send} from "../../main/Main";

let dispatch, teachersInfo, navigate, cState, themeState, inps, errText;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
inps = {inpnpt : "Фамилия И.О."};
let [_, forceUpdate] = [];

function getTea(title, b, b1) {
    return (
        <div className={peopleCSS.nav_iZag}>
            <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                {title}
            </div>
            {b1 ?
                    <>
                        <div className={peopleCSS.add+" "+peopleCSS.nav_iZag} data-st="0">
                            <div className={peopleCSS.nav_i+" "+peopleCSS.link} id={peopleCSS.nav_i} onClick={onEdit}>
                                Добавить педагога
                            </div>
                            <div className={peopleCSS.pepl} data-st="0">
                                <div className={peopleCSS.fi}>
                                    <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                        {inps.inpnpt}
                                    </div>
                                    <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                    <img className={peopleCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_TEACHERS)} title="Подтвердить" alt=""/>
                                    <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                </div>
                                <div className={peopleCSS.ed}>
                                    <div className={peopleCSS.preinf}>
                                        ФИО:
                                    </div>
                                    <input className={peopleCSS.inp} id={"inpnpt_"} placeholder={"Фамилия И.О."} defaultValue={inps.inpnpt} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                    {ele(false, "inpnpt_", inps)}
                                    <img className={peopleCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate)} title="Подтвердить" alt=""/>
                                    <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                </div>
                            </div>
                        </div>
                        {teachersInfo.nt && Object.getOwnPropertyNames(teachersInfo.nt.tea).map((param, i, xs, info = teachersInfo.nt.tea[param]) =>
                            <div className={peopleCSS.nav_iZag + " " + peopleCSS.nav_iZag1} key={param}>
                                <div className={peopleCSS.pepl} data-st="0">
                                    <div className={peopleCSS.fi}>
                                        <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                            {info.name}
                                        </div>
                                        {info.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info.login)} title="Так будет выглядеть иконка перехода в профиль" alt=""/>}
                                        <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                        <img className={peopleCSS.imginp} data-id={"nt_" + param} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onDel(e, CHANGE_TEACHERS_DEL)} title="Удалить" alt=""/>
                                        <input className={peopleCSS.inp+" "+peopleCSS.copyInp} data-id={"nt_" + param} id={"inpcpt_" + param} placeholder="Ссылка не создана" value={info.link ? sit + (info.login ? "/reauth/" : "/invite/") + info.link : undefined} type="text" readOnly/>
                                        <img className={peopleCSS.imginp+" "+peopleCSS.refrC} src={themeState.theme_ch ? refreshCd : refreshCl} onClick={(e)=>refreshLink(e, sit, CHANGE_TEACHERS)} title="Создать ссылку-приглашение" alt=""/>
                                        <img className={peopleCSS.imginp} src={themeState.theme_ch ? copyd : copyl} title="Копировать" data-enable={info.link ? "1" : "0"} onClick={(e)=>copyLink(e, info.link, info.name)} alt=""/>
                                    </div>
                                    <div className={peopleCSS.ed}>
                                        <div className={peopleCSS.preinf}>
                                            ФИО:
                                        </div>
                                        <input className={peopleCSS.inp} data-id={"nt_" + param} id={"inpnpt_nt" + param} placeholder={"Фамилия И.О."} defaultValue={info.name} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                        {ele(false, "inpnpt_nt_" + param, inps)}
                                        <img className={peopleCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_TEACHERS)} title="Подтвердить" alt=""/>
                                        <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                    </div>
                                </div>
                            </div>
                        )}
                    </>
                :
                    Object.getOwnPropertyNames(teachersInfo).map((param, i, xs, info = teachersInfo[param]) =>
                        param != "nt" &&
                        <div className={peopleCSS.nav_iZag + " " + peopleCSS.nav_iZag1} key={param}>
                            <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                                {info.name}
                            </div>
                            {Object.getOwnPropertyNames(info.tea).map((param1, i1, xs1, info1 = info.tea[param1]) =>
                                b ?
                                    <div className={peopleCSS.pepl} key={param1} data-st="0">
                                        <div className={peopleCSS.fi}>
                                            <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                                {info1.name}
                                            </div>
                                            {info1.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info1.login)} title="Так будет выглядеть иконка перехода в профиль" alt=""/>}
                                            <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                            <img className={peopleCSS.imginp} data-id={param + "_" + param1} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onDel(e, CHANGE_TEACHERS_DEL)} title="Удалить" alt=""/>
                                            <input className={peopleCSS.inp+" "+peopleCSS.copyInp} data-id={param + "_" + param1} id={"inpcpt_" + param + "_" + param1} placeholder="Ссылка не создана" value={info1.link ? sit + (info1.login ? "/reauth/" : "/invite/") + info.tea[param1].link : undefined} type="text" readOnly/>
                                            <img className={peopleCSS.imginp+" "+peopleCSS.refrC} src={themeState.theme_ch ? refreshCd : refreshCl} onClick={(e)=>refreshLink(e, sit, CHANGE_TEACHERS)} title="Создать ссылку-приглашение" alt=""/>
                                            <img className={peopleCSS.imginp} src={themeState.theme_ch ? copyd : copyl} title="Копировать" data-enable={info1.link ? "1" : "0"} onClick={(e)=>copyLink(e, info1.link, info1.name)} alt=""/>
                                        </div>
                                        <div className={peopleCSS.ed}>
                                            <div className={peopleCSS.preinf}>
                                                ФИО:
                                            </div>
                                            <input className={peopleCSS.inp} data-id={param + "_" + param1} id={"inpnpt_" + param + "_" + param1} placeholder={"Фамилия И.О."} defaultValue={info1.name} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                            {ele(false, "inpnpt_" + param + "_" + param1, inps)}
                                            <img className={peopleCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_TEACHERS)} title="Подтвердить" alt=""/>
                                            <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                        </div>
                                    </div>
                                :
                                    <div key={param1}>
                                        <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                            {info1.name}
                                        </div>
                                        {info1.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info1.login)} title="Перейти в профиль" alt=""/>}
                                    </div>
                            )}
                        </div>
                    )
            }
        </div>
    );
}

function goToProf(log) {
    if(log) navigate("/profiles/" + log);
}

function addTeaC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(CHANGE_TEACHERS, "nt", msg.id, undefined, msg.name));
}

function codPepL1C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(CHANGE_TEACHERS, msg.id, msg.id1, undefined, msg.code, "link"));
}

export function codTea (id, id1, title, text) {
    console.log("codPar");
    send({
        uuid: cState.uuid,
        id: id,
        id1: id1,
        role: cState.role
    }, 'POST', "teachers", "setCodePep")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
                // dispatch(changePeople(CHANGE_PARENTS_L1, undefined, data.id, undefined, data.body));
            }
        });
}

export function addTea(inp, par) {
    console.log("addTea");
    send({
        uuid: cState.uuid,
        name: inp
    }, 'POST', "teachers", "addTea")
        .then(data => {
            console.log(data);
            if(data.error == false){
                // dispatch(changePeople(CHANGE_TEACHERS, "nt", data.id, undefined, data.name));
                par.setAttribute('data-st', '0');
            }
        });
}

function onCon(e) {
    setInfo();
}

function setInfo() {
    send({
        uuid: cState.uuid
    }, 'POST', "teachers", "getTeachers")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changePeople(CHANGE_TEACHERS_GL, 0, 0, 0, data.body));
            }
        });
}

export function Teachers() {
    teachersInfo = useSelector(teachers);
    themeState = useSelector(themes);
    cState = useSelector(states);
    navigate = useNavigate();
    if(!dispatch) {
        setActNew(0);
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addTeaC', addTeaC, false);
        eventSource.addEventListener('codPepL1C', codPepL1C, false);
    }
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Teachers.jsx");
        for(let el of document.querySelectorAll("." + peopleCSS.ed + " > *[id^='inpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addTeaC', addTeaC);
            eventSource.removeEventListener('codPepL1C', codPepL1C);
            console.log("I was triggered during componentWillUnmount Teachers.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Teachers.jsx');
    });
    return (
        <div className={peopleCSS.header}>
            <Helmet>
                <title>Педагоги</title>
            </Helmet>
            {(cState.auth && cState.role == 3) ?
                    <div className={peopleCSS.blockPep}>
                        <div className={peopleCSS.pep}>
                            {getTea("Нераспределённые педагоги", true, true)}
                            {getTea("Педагоги", true)}
                        </div>
                    </div>
                :
                    Object.getOwnPropertyNames(teachersInfo).length == 0 ?
                            <ErrFound text={errText}/>
                        :
                            <div className={peopleCSS.blockPep}>
                                <div className={peopleCSS.pep}>
                                    {getTea("Мои педагоги")}
                                </div>
                            </div>
            }
        </div>
    )
}
export default Teachers;