import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import analyticsCSS from '../analyticsMain.module.css';
import periodsCSS from './periods.module.css';
import {useDispatch, useSelector} from "react-redux";
import {chStatB, ele, onClose, onDel, onEdit, onFin, setActNew} from "../AnalyticsMain";
import {periods, states} from "../../../store/selector";
import ErrFound from "../../other/error/ErrFound";
import yes from "../../../media/yes.png";
import {
    CHANGE_EVENTS_CLEAR,
    CHANGE_PERIODS,
    CHANGE_PERIODS_DEL,
    CHANGE_PERIODS_GL,
    CHANGE_PERIODS_L1,
    changeAnalytics,
    changeEvents
} from "../../../store/actions";
import no from "../../../media/no.png";
import ed from "../../../media/edit.png";
import {eventSource, sendToServer} from "../../main/Main";
import {cPeriods} from "../../other/Controllers";

let dispatch, periodsInfo, errText, cState, inps;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
inps = {inpnnt : "V четверть", inpnit: "01.09.22", inpnit1: "03.11.22"};
let [_, forceUpdate] = [];

function addPerC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeAnalytics(CHANGE_PERIODS_L1, "prs", msg.id, undefined, msg.body));
}

export function addPer (name, perN, perK) {
    console.log("addPer");
    sendToServer({
        name: name,
        perN: perN,
        perK: perK
    }, 'POST', cPeriods + "addPer")
}

function onCon(e) {
    setInfo();
}

function setInfo() {
    sendToServer(0, 'GET', cPeriods + "getInfo")
        .then(data => {
            console.log(data);
            if(data.status == 200){
                dispatch(changeAnalytics(CHANGE_PERIODS_GL, 0, 0, 0, data.body.bodyP));
                for(let el of document.querySelectorAll(" *[id^='inpn']")){
                    chStatB({target: el}, inps, forceUpdate);
                }
            }
        });
}

export function Periods() {
    periodsInfo = useSelector(periods);
    cState = useSelector(states);
    if(!dispatch) setActNew(1);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Periods.jsx");
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addPerC', addPerC, false);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addPerC', addPerC);
            console.log("I was triggered during componentWillUnmount Periods.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Periods.jsx');
    });
    return <div className={analyticsCSS.header}>
        <Helmet>
            <title>Расписание периодов</title>
        </Helmet>
        {!Object.getOwnPropertyNames(periodsInfo).length ?
                <ErrFound text={errText}/>
            :
                <div className={analyticsCSS.block}>
                    <div className={analyticsCSS.l1+" "+periodsCSS.per}>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            №
                        </div>
                        <div className={analyticsCSS.nav_i} style={{gridColumn: "2"}} id={analyticsCSS.nav_i}>
                            Название учебного периода
                        </div>
                        <div className={analyticsCSS.nav_i} style={{gridColumn: "3"}} id={analyticsCSS.nav_i}>
                            Начало периода
                        </div>
                        <div className={analyticsCSS.nav_i} style={{gridColumn: "4"}} id={analyticsCSS.nav_i}>
                            Конец периода
                        </div>
                        {Object.getOwnPropertyNames(periodsInfo).map((param, i) =>
                            <>
                                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                                    {i + 1}
                                </div>
                                <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                                    <div className={analyticsCSS.fi}>
                                        <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                            {periodsInfo[param].name}
                                        </div>
                                        <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                    </div>
                                    <div className={analyticsCSS.ed}>
                                        <div className={analyticsCSS.preinf}>
                                            Название:
                                        </div>
                                        <input className={analyticsCSS.inp} data-id={param + "_name"} id={"inpnnt_" + param + "_name"} placeholder={"X Смена"} defaultValue={periodsInfo[param].name} onChange={e=>chStatB(e, inps)} type="text"/>
                                        {ele(false, "inpnnt_" + param + "_name", inps)}
                                        <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, inps, forceUpdate, CHANGE_PERIODS)} title="Подтвердить" alt=""/>
                                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                    </div>
                                </div>
                                <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                                    <div className={analyticsCSS.fi}>
                                        <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                            {periodsInfo[param].perN}
                                        </div>
                                        <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                    </div>
                                    <div className={analyticsCSS.ed}>
                                        <div className={analyticsCSS.preinf}>
                                            Дата:
                                        </div>
                                        <input className={analyticsCSS.inp} data-id={param + "_perN"} id={"inpnit_" + param + "_per"} placeholder={"01.09.22"} defaultValue={periodsInfo[param].perN} onChange={e=>chStatB(e, inps)} type="text"/>
                                        {ele(false, "inpnit_" + param + "_perN", inps)}
                                        <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, inps, forceUpdate, CHANGE_PERIODS)} title="Подтвердить" alt=""/>
                                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                    </div>
                                </div>
                                <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                                    <div className={analyticsCSS.fi}>
                                        <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                            {periodsInfo[param].perK}
                                        </div>
                                        <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={e=>onDel(e, CHANGE_PERIODS_DEL)} title="Удалить" alt=""/>
                                    </div>
                                    <div className={analyticsCSS.ed}>
                                        <div className={analyticsCSS.preinf}>
                                            Дата:
                                        </div>
                                        <input className={analyticsCSS.inp} data-id={param + "_perK"} id={"inpnit1_" + param + "_perK"} placeholder={"03.11.22"} defaultValue={periodsInfo[param].perK} onChange={e=>chStatB(e, inps)} type="text"/>
                                        {ele(false, "inpnit1_" + param + "_perK", inps)}
                                        <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, inps, forceUpdate, CHANGE_PERIODS)} title="Подтвердить" alt=""/>
                                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                    </div>
                                </div>
                            </>
                        )}
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            X
                        </div>
                        <div className={analyticsCSS.add} data-st={"0"} style={{gridColumn: "2/5"}}>
                            <div className={analyticsCSS.nav_i+" "+analyticsCSS.link} id={analyticsCSS.nav_i} onClick={onEdit}>
                                Добавить период
                            </div>
                            <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                                <div className={analyticsCSS.preinf}>
                                    Название:
                                </div>
                                <input className={analyticsCSS.inp} id={"inpnnt_"} placeholder={"X Смена"} defaultValue={inps.inpnnt} onChange={e=>chStatB(e, inps, forceUpdate)} type="text"/>
                                {ele(false, "inpnnt_", inps)}
                                <div className={analyticsCSS.preinf}>
                                    , Дата:
                                </div>
                                <input className={analyticsCSS.inp} id={"inpnit_"} placeholder={"01.09.22"} defaultValue={inps.inpnit} onChange={e=>chStatB(e, inps, forceUpdate)} type="text"/>
                                {ele(false, "inpnit_", inps)}
                                <div className={analyticsCSS.preinf}>
                                    , Дата:
                                </div>
                                <input className={analyticsCSS.inp} id={"inpnit1_"} placeholder={"03.11.22"} defaultValue={inps.inpnit1} onChange={e=>chStatB(e, inps, forceUpdate)} type="text"/>
                                {ele(false, "inpnit1_", inps)}
                                <img className={analyticsCSS.imginp} data-enable={inps.inpnnt_ && inps.inpnit_ && inps.inpnit1_ ? "1" : "0"} src={yes} onClick={e=>onFin(e, inps, forceUpdate, CHANGE_PERIODS_L1, periodsInfo)} title="Подтвердить" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                            </div>
                        </div>
                    </div>
                </div>
        }
    </div>
}
export default Periods;