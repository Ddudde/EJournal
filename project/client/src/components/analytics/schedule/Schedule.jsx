import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import analyticsCSS from '../analyticsMain.module.css';
import scheduleCSS from './schedule.module.css';
import {groups, schedules, states, teachers, themes} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {eventSource, send, setActived} from "../../main/Main";
import {chStatB, ele, onClose, onEdit, setActNew} from "../AnalyticsMain";
import Pane from "../../other/pane/Pane";
import ErrFound from "../../other/error/ErrFound";
import yes from "../../../media/yes.png";
import {
    CHANGE_EVENTS_CLEAR,
    CHANGE_GROUPS_GL,
    CHANGE_GROUPS_GR,
    CHANGE_SCHEDULE,
    CHANGE_SCHEDULE_DEL,
    CHANGE_SCHEDULE_GL,
    CHANGE_SCHEDULE_PARAM,
    CHANGE_TEACHERS_GL,
    changeAnalytics,
    changeEvents,
    changeGroups,
    changePeople
} from "../../../store/actions";
import no from "../../../media/no.png";
import mapd from "../../../media/Map_symbolD.png";
import mapl from "../../../media/Map_symbolL.png";
import ed from "../../../media/edit.png";

let dispatch, cState, selGr, schedulesInfo, groupsInfo, errText, inps, teachersInfo, themeState, DoW;
DoW = ["Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"];
inps = {sinpnpt : "Математика", sinpnkt: "300"};
selGr = 0;
let [_, forceUpdate] = [];
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";

function onDel(e, info) {
    let par, inp;
    par = e.target.parentElement.parentElement;
    if(par.classList.contains(analyticsCSS.edbl)){
        inp = par.querySelector("input");
        if(!inp){
            dispatch(changeAnalytics(CHANGE_SCHEDULE_DEL, info.id, info.id1));
        }
    }
}

function onFin(e, type, info) {
    let par, inp;
    par = e.target.parentElement;
    inp = par.querySelector("input");
    if(par.classList.contains(analyticsCSS.edbl)){
        if(type == CHANGE_SCHEDULE){
            let inpm = ["sinpnpt_", "sinpnkt_"];
            if(inps.sinpnpt_ && inps.sinpnkt_ && inps.nyid)
            {
                let obj, param;
                param = inp.getAttribute("data-id1");
                obj = {
                    name: inps.sinpnpt_,
                    cabinet: inps.sinpnkt_,
                    prepod: {
                        name: inps.nw.prepod,
                        id: inps.nyid
                    }
                }
                addLesson(param, obj);
                // dispatch(changeAnalytics(type, param, id, undefined, obj));
            } else {
                for(let i = 0, inpf; i < inpm.length; i++) {
                    inpf = document.querySelector("." + analyticsCSS.edbl + " *[id='" + inpm[i] + "']")
                    inpf.setAttribute("data-mod", '1');
                }
            }
            return;
        }
    }
    if(!inp){
        if(type == CHANGE_SCHEDULE_PARAM) {
            let obj = {
                name: inps.nw.prepod,
                id: inps.nyid
            }
            dispatch(changeAnalytics(type, info.id, info.id1, info.par, obj));
        }
        par = par.parentElement;
        par.setAttribute('data-st', '0');
        return;
    }
    if (inps[inp.id]) {
        inp.setAttribute("data-mod", '0');
        if(par.parentElement.classList.contains(analyticsCSS.edbl)) {
            par = par.parentElement;
            if(type){
                if(inp.hasAttribute("data-id")){
                    let id = inp.getAttribute("data-id").split("_");
                    if(type == CHANGE_SCHEDULE_PARAM) {
                        dispatch(changeAnalytics(type, id[0], id[1], info, inp.value));
                    }
                }
            }
        }
        par.setAttribute('data-st', '0');
    } else {
        inp.setAttribute("data-mod", '1');
    }
}

function getSched(dI, b) {
    if(dI.length < 7) {
        for(let i = dI.length; i < 7; i++){
            dI[i] = "";
        }
    }
    return b ?
        dI.map((param, i, x, dai = schedulesInfo[param], dLI = (dai ? Object.getOwnPropertyNames(dai):[])) =>
            <div className={analyticsCSS.l1+" "+scheduleCSS.day}>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                    №
                </div>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "2"}}>
                    {DoW[i]}
                </div>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "3"}}>
                    Кабинет
                </div>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "4"}}>
                    Преподаватель
                </div>
                {dLI.map((param1, i1, x, les = dai[param1]) =>
                    <>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            {i1 + 1}
                        </div>
                        <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                            <div className={analyticsCSS.fi}>
                                <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                    {les.name}
                                </div>
                                <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                            </div>
                            <div className={analyticsCSS.ed}>
                                <div className={analyticsCSS.preinf}>
                                    Предмет:
                                </div>
                                <input className={analyticsCSS.inp} data-id={param + "_" + param1} id={"sinpnpt_" + param + "_" + param1} placeholder={"Математика"} defaultValue={les.name} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                {ele(false, "sinpnpt_" + param + "_" + param1, inps)}
                                <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, CHANGE_SCHEDULE_PARAM, "name")} title="Подтвердить" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                            </div>
                        </div>
                        <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                            <div className={analyticsCSS.fi}>
                                <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                    {les.cabinet}
                                </div>
                                <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                            </div>
                            <div className={analyticsCSS.ed}>
                                <div className={analyticsCSS.preinf}>
                                    Кабинет:
                                </div>
                                <input className={analyticsCSS.inp} data-id={param + "_" + param1} id={"sinpnkt_" + param + "_" + param1} placeholder={"300"} defaultValue={les.cabinet} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                {ele(false, "sinpnkt_" + param + "_" + param1, inps)}
                                <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, CHANGE_SCHEDULE_PARAM, "cabinet")} title="Подтвердить" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                            </div>
                        </div>
                        <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                            <div className={analyticsCSS.fi}>
                                <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                    {les.prepod.name}
                                </div>
                                <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={e=>onDel(e, {id: param, id1: param1})} title="Удалить" alt=""/>
                            </div>
                            <div className={analyticsCSS.ed}>
                                <div className={analyticsCSS.preinf}>
                                    Педагог:
                                </div>
                                {getPrep(param)}
                                <img className={analyticsCSS.imginp} data-enable={inps.nw && inps.nw.prepod ? "1" : "0"} src={yes} onClick={e=>onFin(e, CHANGE_SCHEDULE_PARAM, {par: "prepod", id: param, id1: param1, st: schedulesInfo})} title="Подтвердить" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                            </div>
                        </div>
                    </>
                )}
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                    X
                </div>
                <div className={analyticsCSS.add} data-st="0" style={{gridColumn: "2/5"}}>
                    <div className={analyticsCSS.nav_i+" "+analyticsCSS.link} id={analyticsCSS.nav_i} onClick={onEdit}>
                        Добавить урок
                    </div>
                    <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                        <div className={analyticsCSS.preinf}>
                            Предмет:
                        </div>
                        <input className={analyticsCSS.inp} data-id1={param} id={"sinpnpt_"} placeholder={"Математика"} defaultValue={inps.sinpnpt} onChange={(e)=>chStatB(e, inps, forceUpdate)} type="text"/>
                        {ele(false, "sinpnpt_", inps)}
                        <div className={analyticsCSS.preinf}>
                            , Кабинет:
                        </div>
                        <input className={analyticsCSS.inp} data-id1={param} id={"sinpnkt_"} placeholder={"300"} defaultValue={inps.sinpnkt} onChange={(e)=>chStatB(e, inps, forceUpdate)} type="text"/>
                        {ele(false, "sinpnkt_", inps)}
                        <div className={analyticsCSS.preinf}>
                            , Педагог:
                        </div>
                        {getPrep(param)}
                        <img className={analyticsCSS.imginp} data-enable={inps.sinpnpt_ && inps.sinpnkt_ && inps && inps.nw && inps.nw.prepod ? "1" : "0"} src={yes} onClick={e=>onFin(e, CHANGE_SCHEDULE, schedulesInfo)} title="Подтвердить" alt=""/>
                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                    </div>
                </div>
                {dLI.length < 4 && Array(4-dLI.length).fill('').map(param =>
                    <>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                    </>
                )}
            </div>
        )
    :
        dI.map((param, i, x, dLI = (schedulesInfo[param] ? Object.getOwnPropertyNames(schedulesInfo[param]):[])) =>
            <div className={analyticsCSS.l1+" "+scheduleCSS.day}>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                    №
                </div>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "2"}}>
                    {DoW[i]}
                </div>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "3"}}>
                    Кабинет
                </div>
                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "4"}}>
                    {cState.role == 2 ? "Группа" : "Преподаватель"}
                </div>
                {dLI.map((param1, i1, x, les = schedulesInfo[param][param1]) =>
                    <>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            {i1 + 1}
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            {les.name}
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            {les.cabinet}
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            {cState.role == 2 ? les.group : les.prepod.name}
                        </div>
                    </>
                )}
                {dLI.length < 5 && Array(5-dLI.length).fill('').map(param =>
                    <>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            <br />
                        </div>
                    </>
                )}
            </div>
        )
}

function selecPrep(e, id, obj) {
    inps.nyid = id;
    if(!inps.nw) inps.nw = {};
    inps.nw.prepod = obj.name;
    forceUpdate();
    // dispatch(changeAnalytics(CHANGE_SCHEDULE_PARAM, param, "nw", "prepod", obj.name));
}

function getPrep(param) {
    let ltI0 = Object.getOwnPropertyNames(teachersInfo);
    return (
        <div className={scheduleCSS.blockList}>
            <div className={analyticsCSS.nav_i+' '+scheduleCSS.selEl} id={analyticsCSS.nav_i}>
                <div className={scheduleCSS.elInf}>Педагог:</div>
                <div className={scheduleCSS.elText}>{inps && inps.nw && inps.nw.prepod ? inps.nw.prepod : "Не выбран"}</div>
                <img className={scheduleCSS.mapImg} data-enablem={ltI0.length < 2 ? "0" : "1"} src={themeState.theme_ch ? mapd : mapl} alt=""/>
            </div>
            <div className={scheduleCSS.list}>
                {ltI0.map((param1, i, x, info = teachersInfo[param1], lltI = Object.getOwnPropertyNames(teachersInfo[param1].tea)) =>
                    <>
                        {lltI.length > 0 && !(lltI.length == 1 && lltI[0] == inps.nyid) &&
                            <div className={analyticsCSS.nav_i+' '+scheduleCSS.listZag} id={analyticsCSS.nav_i}>
                                <div className={scheduleCSS.elInf}>{param1 == "nt" ? "Нераспределённые" : info.name}:</div>
                            </div>
                        }
                        {lltI.map((param2, i, x, tO = info.tea[param2]) =>
                            param2 != inps.nyid &&
                            <div className={analyticsCSS.nav_i+' '+scheduleCSS.listEl} key={param2} id={analyticsCSS.nav_i} onClick={e => (selecPrep(e, param2, tO))}>
                                <div className={scheduleCSS.elInf}>Педагог:</div>
                                <div className={scheduleCSS.elText}>{tO.name}</div>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

function onCon(e) {
    setInfo();
}

function addLessonC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeAnalytics(CHANGE_SCHEDULE, msg.day, msg.id, undefined, msg.body));
}

function addLesson(day, obj) {
    send({
        uuid: cState.uuid,
        group: groupsInfo.group,
        day: day,
        obj: obj
    }, 'POST', "schedule", "addLesson");
}

function setInfo() {
    send({
        uuid: cState.uuid
    }, 'POST', "schedule", "getInfo")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changeGroups(CHANGE_GROUPS_GL, undefined, data.bodyG));
                if(!data.bodyG[groupsInfo.group]) {
                    dispatch(changeGroups(CHANGE_GROUPS_GR, undefined, parseInt(data.firstG)));
                }
                dispatch(changePeople(CHANGE_TEACHERS_GL, 0, 0, 0, data.bodyT));
                setSchedule();
            }
        });
}

function setSchedule() {
    send({
        uuid: cState.uuid,
        group: groupsInfo.group
    }, 'POST', "schedule", "getSchedule")
        .then(data => {
            console.log(data);
            if(data.error == false){
                selGr = groupsInfo.group;
                dispatch(changeAnalytics(CHANGE_SCHEDULE_GL, 0, 0, 0, data.body));
            }
        });
}

export function Schedule() {
    schedulesInfo = useSelector(schedules);
    teachersInfo = useSelector(teachers);
    groupsInfo = useSelector(groups);
    themeState = useSelector(themes);
    cState = useSelector(states);
    if(!dispatch && cState.role != 2) {
        setActNew(2);
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addLessonC', addLessonC, false);
    }
    if(!dispatch && cState.role == 2) setActived(8);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Schedule.jsx");
        for(let el of document.querySelectorAll(" *[id^='sinpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addLessonC', addLessonC);
            console.log("I was triggered during componentWillUnmount Schedule.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(selGr != groupsInfo.group){
            if(eventSource.readyState == EventSource.OPEN) setSchedule();
        }
        console.log('componentDidUpdate Schedule.jsx');
    });
    let dI = Object.getOwnPropertyNames(schedulesInfo);
    return (
        <div className={analyticsCSS.header}>
            <Helmet>
                <title>Расписание</title>
            </Helmet>
            {dI.length == 0 && cState.role != 3 ?
                    <ErrFound text={errText}/>
                :
                    <>
                        {(cState.auth && cState.role == 3) &&
                            <div className={scheduleCSS.pane}>
                                <Pane cla={true}/>
                            </div>
                        }
                        <div className={analyticsCSS.block} style={{marginTop: (cState.auth && cState.role == 3) ? "7vh" : undefined}}>
                            {getSched(dI, cState.role == 3)}
                        </div>
                    </>
            }
        </div>
    )
}
export default Schedule;