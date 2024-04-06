import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import journalCSS from './journal.module.css';
import {journals, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {setActNew} from "../AnalyticsMain";
import ErrFound from "../../other/error/ErrFound";
import {
    CHANGE_EVENTS_CLEAR,
    CHANGE_EVENTS_RL,
    CHANGE_JOURNAL_GL,
    changeEvents,
    changeJournal
} from "../../../store/actions";
import {eventSource, sendToServer} from "../../main/Main";
import {cJournal} from "../../other/Controllers";

let dispatch, journalsInfo, maxEl, errText, cState, selKid;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
maxEl = 0;

function getDate(dat) {
    let d = dat.split('.');
    return new Date("20" + [d[2], d[1], d[0]].join("-"));
}

function updDateP(id) {
    let mas, lMonth;
    for(let el of document.querySelectorAll("." + journalCSS.daysGrid + " #" + journalCSS.nav_i)) {
        el.innerHTML = "";
    }
    if(!journalsInfo[id] || !journalsInfo[id].days) return;
    mas = Object.getOwnPropertyNames(journalsInfo[id].days);
    lMonth = 0;
    for(let i = 0; i < mas.length; i++) {
        let date, month, dat, el;
        date = getDate(mas[i]);
        month = date.toLocaleString("ru", {month:"2-digit"});
        dat = date.toLocaleString("ru", month == lMonth ? {day:"2-digit"} : {day:"2-digit", month:"short"});
        lMonth = month;
        el = document.querySelector("." + journalCSS.daysGrid + " div:nth-child(" + (i + 2) + ")");
        if(el) el.innerHTML = dat;
    }
}

function updDate(e) {
    updDateP(this.id);
}

function onCon(e) {
    setInfo();
}

function setInfo() {
    sendToServer({
        uuid: cState.uuid
    }, 'POST', cJournal + "getInfo")
        .then(data => {
            console.log(data);
            if(data.error == false){
                let weight, sum, mar, wei;
                for(let predm in data.bodyJ) {
                    weight = 0;
                    sum = 0;
                    for(let day in data.bodyJ[predm].days) {
                        mar = parseInt(data.bodyJ[predm].days[day].mark);
                        if(!mar || isNaN(mar)) continue;
                        wei = parseInt(data.bodyJ[predm].days[day].weight);
                        if(!wei) wei = 1;
                        sum += mar*wei;
                        weight += wei;
                    }
                    if (sum && weight) {
                        if(!data.bodyJ[predm].avg) data.bodyJ[predm].avg = {};
                        data.bodyJ[predm].avg.mark = (sum/weight).toFixed(2);
                    }
                }
                if(cState.role == 1 && cState.kid) selKid = cState.kid;
                dispatch(changeJournal(CHANGE_JOURNAL_GL, 0, data.bodyJ));
            }
        });
}

export function AnalyticsJournal() {
    journalsInfo = useSelector(journals);
    cState = useSelector(states);
    let jourInf;
    jourInf = Object.getOwnPropertyNames(journalsInfo);
    for(let el of jourInf){
        if(!journalsInfo[el].days) continue;
        let len = Object.getOwnPropertyNames(journalsInfo[el].days).length;
        if(len > maxEl) maxEl = len;
    }
    if(!dispatch) setActNew(3);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount AnalyticsJournal.jsx");
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        // dispatch(changeJournal("Англ. яз.", obj));
        // document.querySelector("." + journalCSS.predm).addEventListener('mouseover', updDate, {capture: true});
        for(let el of document.querySelectorAll("div[class='" + journalCSS.predmGrid+"']")) {
            el.addEventListener('mouseover', updDate);
        }
        updDateP(jourInf[0]);
        let scr = document.querySelector("." + journalCSS.days);
        if(scr) scr.scrollTo(scr.scrollWidth, 0);
        dispatch(changeEvents(CHANGE_EVENTS_RL, false));
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch(changeEvents(CHANGE_EVENTS_RL, true));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            console.log("I was triggered during componentWillUnmount AnalyticsJournal.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(cState.role == 1 && cState.kid && selKid != cState.kid) {
            selKid = cState.kid;
            setInfo();
        }
        console.log('componentDidUpdate AnalyticsJournal.jsx');
    });
    return <div className={journalCSS.AppHeader}>
        <Helmet>
            <title>Журнал</title>
        </Helmet>
        {!jourInf.length ?
                <ErrFound text={errText}/>
            :
                <div className={journalCSS.blockPredm}>
                    <div className={journalCSS.predm}>
                        <div className={journalCSS.days}>
                            <div className={journalCSS.nav_i+" "+journalCSS.namd} id={journalCSS.nav_i}>
                                <br/>
                            </div>
                            <div className={journalCSS.daysGrid}>
                                <div className={journalCSS.nav_i} id={journalCSS.nav_i}>
                                    <br/>
                                </div>
                                {Array(maxEl).fill('').map(param =>
                                    <div className={journalCSS.nav_i+" "+journalCSS.nav_iTextD} id={journalCSS.nav_i}>
                                        <br/>
                                    </div>
                                )}
                                <div className={journalCSS.nav_i}>
                                    <div className={journalCSS.nav_iText}>
                                        Средняя
                                    </div>
                                </div>
                            </div>
                            {jourInf.map((param, i, x, days = journalsInfo[param].days ? Object.getOwnPropertyNames(journalsInfo[param].days) : []) =>
                                <div className={journalCSS.predmGrid} id={param}>
                                    <div className={journalCSS.nav_i+" nam " + journalCSS.nam} id={journalCSS.nav_i}>
                                        {param}
                                    </div>
                                    <div className={journalCSS.nav_i+" "+journalCSS.nav_iBr} id={journalCSS.nav_i}>
                                        <br/>
                                    </div>
                                    {days.map((param1, i1, x1, les = journalsInfo[param].days[param1]) =>
                                        <div className={journalCSS.nav_i + " " + journalCSS.blockMark} id={journalCSS.nav_i} data-tooltip={les.type}>
                                            {les.mark}
                                            {les.weight > 1 && <div className={journalCSS.nav_iWeight}>
                                                {les.weight}
                                            </div>}
                                        </div>)}
                                    {days.length < maxEl && Array(maxEl-days.length).fill('').map(param =>
                                        <div className={journalCSS.nav_i} id={journalCSS.nav_i}>
                                            <br/>
                                        </div>
                                    )}
                                    {<div className={journalCSS.nav_i + " " + journalCSS.nav_iTextM} style={{fontSize: "0.85vw"}}>
                                        {journalsInfo[param]?.avg?.mark || <br/>}
                                    </div>}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
        }
    </div>
}
export default AnalyticsJournal;