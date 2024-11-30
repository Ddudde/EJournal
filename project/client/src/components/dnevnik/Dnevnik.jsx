import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import dnevCSS from './dnevnik.module.css';
import {dnevnik, schedules, states} from "../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {
    CHANGE_DNEVNIK,
    CHANGE_EVENTS_CLEAR,
    CHANGE_EVENTS_RL,
    CHANGE_SCHEDULE_GL,
    changeAnalytics,
    changeDnevnik,
    changeEvents
} from "../../store/actions";
import knopka from "../../media/dnevnik/knopka.png";
import {eventSource, sendToServer, setActived} from "../main/Main";
import ErrFound from "../other/error/ErrFound";
import {cDnevnik} from "../other/Controllers";

let ev, dnev, dispatch, selKid, timid, cState, schedulesInfo, errText, DoW, scrolling, days, elem;
DoW = ["Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"];
elem = {CW: undefined, CW1: undefined};
scrolling = false;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
days = Array(7).fill('');
let [_, forceUpdate] = [];

function tim() {
    if (scrolling) {
        scrolling = false;
        if(ev.deltaY < 0 && window.pageYOffset == 0) {
            let i = dnev.reqWeek[0] - 1;
            dnev.reqWeek.splice(0, 0, i);
            forceUpdate();
        }
        if(ev.deltaY > 0 && window.pageYOffset >= (document.body.scrollHeight-document.body.clientHeight)) {
            let i = dnev.reqWeek[dnev.reqWeek.length-1] + 1;
            dnev.reqWeek.push(i);
            forceUpdate();
        }
        knop();
    }
}

function knop() {
    let x, x1;
    if(!elem.CW1 || !elem.CW) return;
    x = elem.CW1.getBoundingClientRect().top + Math.round(window.innerHeight / 100) * 7 - window.innerHeight;
    x1 = elem.CW.getBoundingClientRect().top + Math.round(window.innerHeight / 100) * 7 - window.innerHeight;
    document.querySelector("#CWSEL").style.display = x > 0 && x1 < 0 ? "none" : "flex";
}

function getDate(dat) {
    let d = dat.split('.');
    return new Date("20" + [d[2], d[1], d[0]].join("-"));
}

function getDiff(dat, dat1) {
    return (getDate(dat) - getDate(dat1)) > 0;
}

function getDay(dif) {
    let day, date;
    date = new Date();
    day = (date.getDay() || 7) - 1;
    if(day != undefined) date.setDate(date.getDate() - day + dif);
    return date.toLocaleString("ru", {day:"2-digit", month: "2-digit", year:"2-digit"});
}

function getLessons(dLI, dayDate, day, dai, minLess) {
    if(!dnev.min || !dnev.max) return;
    let lel, mas, numLess, weekends;
    weekends = getDiff(dnev.min, dayDate) || getDiff(dayDate, dnev.max);
    lel = weekends ? 0 : parseInt(dLI[dLI.length-1]);
    mas = Array(lel > (minLess-1) ? lel : minLess).fill('');
    if(!weekends) {
        for (numLess of dLI) {
            mas[numLess] = numLess;
        }
    }
    dLI = mas;
    return dLI.map((param2, lesNum, x, les = dai.lessons[param2] || {}, lesDM = (dnev.jur[les.name] || {})[dayDate] || {}, lesD = lesDM[lesDM.i++] || {}) => <>
        <div className={dnevCSS.nav_i} id={dnevCSS.nav_i}>
            {les.name || <br/>}
        </div>
        <div className={dnevCSS.nav_i+" "+dnevCSS.dayHomework} id={dnevCSS.nav_i}>
            {lesD.homework || <br/>}
        </div>
        <div className={dnevCSS.nav_i} id={dnevCSS.nav_i}>
            {lesD.mark || <br/>}
            {lesD.weight > 1 && <div className={dnevCSS.nav_i+" "+dnevCSS.nav_iWeight} id={dnevCSS.nav_i}>
                {lesD.weight}
            </div>}
            {lesD.type && <div className={dnevCSS.nav_i+" "+dnevCSS.nav_iType} id={dnevCSS.nav_i}>
                {lesD.type}
            </div>}
        </div>
    </>)
}

function setDnevnik() {
    sendToServer(0, 'GET', cDnevnik+"getDnevnik")
        .then(data => {
            console.log(data);
            if(data.status == 200) {
                if(cState.role == 1 && cState.kid) selKid = cState.kid;
                dispatch(changeAnalytics(CHANGE_SCHEDULE_GL, 0, 0, 0, data.body.body));
                dispatch(changeDnevnik(CHANGE_DNEVNIK, "jur", data.body.bodyD || {}));
                dispatch(changeDnevnik(CHANGE_DNEVNIK, "min", data.body.min));
                dispatch(changeDnevnik(CHANGE_DNEVNIK, "max", data.body.max));
            }
        });
}

function setInfo() {
    sendToServer(0, 'GET', cDnevnik+"getInfo")
        .then(data => {
            console.log(data);
            if(data.status == 200) setDnevnik();
        });
}

function onCon(e) {
    setInfo();
}

function goTo() {
    if(elem.CW) elem.CW.scrollIntoView(true);
    let sinc = window.scrollY - Math.round(window.innerHeight / 100) * 7;
    window.scrollTo(0, sinc);
    knop();
}

export function Dnevnik() {
    schedulesInfo = useSelector(schedules);
    dnev = useSelector(dnevnik);
    cState = useSelector(states);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Dnevnik.jsx");
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        window.onwheel = e => {
            if(!scrolling) {
                scrolling = true;
                ev = e;
                timid = setTimeout(tim,1000);
            }
        };
        knop();
        setActived(13);
        dispatch(changeEvents(CHANGE_EVENTS_RL, false));
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch(changeEvents(CHANGE_EVENTS_RL, true));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            window.onwheel = undefined;
            clearTimeout(timid);
            console.log("I was triggered during componentWillUnmount Dnevnik1.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(cState.role == 1 && cState.kid && selKid != cState.kid) {
            selKid = cState.kid;
            setDnevnik();
        }
        console.log('componentDidUpdate Dnevnik.jsx');
    });
    return <div className={dnevCSS.AppHeader}>
        <Helmet>
            <title>Дневник</title>
        </Helmet>
        {!Object.getOwnPropertyNames(dnev.jur) ?
                <ErrFound text={errText}/>
            :
                <div className={dnevCSS.blockDay}>
                    {dnev.reqWeek.map(week => <>
                        {<div className={dnevCSS.blockL+" "+dnevCSS.blockLU}>
                            <div className={dnevCSS.blockLine} ref={el=>(week ? el : elem.CW=el)}/>
                            <div className={dnevCSS.blockLText}>
                                {week ? "Неделя " + (week > 0 ? "+" : "") + week : "Текущая неделя"}
                            </div>
                        </div>}
                        {days.map((param1, day, x, dayDate = getDay(day + 7*week), dai = schedulesInfo[day] || {lessons:{}}, dLI = Object.getOwnPropertyNames(dai.lessons)) => <>
                            <div className={dnevCSS.day}>
                                <div className={dnevCSS.nav_i} id={dnevCSS.nav_i}>
                                    {DoW[day]} / {dayDate}
                                </div>
                                <div className={dnevCSS.nav_i} id={dnevCSS.nav_i}>
                                    Домашнее задание
                                </div>
                                <div className={dnevCSS.nav_i} id={dnevCSS.nav_i}>
                                    Оценка
                                </div>
                                {getLessons(dLI, dayDate, day, dai, 5)}
                            </div>
                        </>)}
                        {<div className={dnevCSS.blockL+" "+dnevCSS.blockLD}>
                            <div className={dnevCSS.blockLText+" "+dnevCSS.blockLTextD}>
                                {week ? "Неделя " + (week > 0 ? "+" : "") + week : "Текущая неделя"}
                            </div>
                            <div className={dnevCSS.blockLine} ref={el=>(week ? el : elem.CW1=el)}/>
                        </div>}
                    </>)}
                    <div className={dnevCSS.GotCW} id={"CWSEL"}>
                        <div>
                            <img src={knopka} alt="" onClick={goTo}/>
                            <div className={dnevCSS.GotCWText}>
                                Перейти к текущей неделе
                            </div>
                        </div>
                    </div>
                </div>
        }
    </div>
}
export default Dnevnik;