import React, {useEffect, useRef} from "react";
import journalCSS from './journal.module.css';
import {Helmet} from "react-helmet-async";
import {useDispatch, useSelector} from "react-redux";
import {groups, pjournal, schedules, states, themes} from "../../store/selector";
import {eventSource, sendToServer, setActived} from "../main/Main";
import mapd from "../../media/Map_symbolD.png";
import mapl from "../../media/Map_symbolL.png";
import {
    CHANGE_EVENTS_CLEAR,
    CHANGE_EVENTS_RL,
    CHANGE_GROUPS_GL,
    CHANGE_GROUPS_GR,
    CHANGE_PJOURNAL,
    CHANGE_PJOURNAL_DEL_MARKS,
    CHANGE_PJOURNAL_DEL_PER_MARKS,
    CHANGE_PJOURNAL_GL,
    CHANGE_PJOURNAL_MARKS,
    CHANGE_PJOURNAL_PER_MARKS,
    CHANGE_SCHEDULE_GL,
    changeAnalytics,
    changeDZ,
    changeEvents,
    changeGroups,
    changeJType,
    changePjournal
} from "../../store/actions";
import erad from "../../media/eraserd.png";
import eral from "../../media/eraserl.png";
import no from "../../media/no.png";
import ed from "../../media/edit.png";
import yes from "../../media/yes.png";
import Pane from "../other/pane/Pane";
import ErrFound from "../other/error/ErrFound";
import {cPjournal} from "../other/Controllers";

let jourInfo, errText, groupsInfo, cState, schedulesInfo, dispatch, theme, pari, parb, inps, days, lastD, lastDI, selGr;
pari = {lMonth: 0};
parb = {upddel: false};
inps = {};
selGr = 0;
days = [];
lastDI = 0;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";

function selecPredm(param) {
    dispatch(changePjournal(CHANGE_PJOURNAL, "predm", param));
    setInfoP2(jourInfo.predms[param]);
}

function getPredms(predmsK) {
    return <div className={journalCSS.blockList}>
        <div className={journalCSS.nav_i+' '+journalCSS.selEl} id={journalCSS.nav_i}>
            <div className={journalCSS.elInf}>Предмет:</div>
            {jourInfo.predms && <div className={journalCSS.elText}>{jourInfo.predms[jourInfo.predm]}</div>}
            <img className={journalCSS.mapImg} src={theme.theme_ch ? mapd : mapl} alt=""/>
        </div>
        <div className={journalCSS.list}>
            {jourInfo.predms && predmsK.map(param1 =>
                param1 != jourInfo.predm && <div className={journalCSS.nav_i+' '+journalCSS.listEl} id={journalCSS.nav_i} onClick={e => selecPredm(param1)}>
                    <div className={journalCSS.elInf}>Предмет:</div>
                    <div className={journalCSS.elText}>{jourInfo.predms[param1]}</div>
                </div>
            )}
        </div>
    </div>
}

function trEnd(e) {
    if(e.propertyName != "opacity") return;
    if (this.parentElement.matches(':hover')) {
        this.removeAttribute("data-tr");
    } else {
        this.setAttribute("data-tr", "");
    }
}

function ele(x, par, b) {
    if(b){
        if(!inps[par]) inps[par] = x;
    } else {
        pari[par] = x;
    }
}

function onEdit(e, dbut) {
    let par = e.target.parentElement;
    if(dbut) par = document.querySelector("." + journalCSS.AppHeader);
    par.setAttribute('data-st', '1');
}

function onFin(e) {
    let par, inp, bo;
    par = e.target.parentElement;
    inp = par.querySelectorAll("." + journalCSS.inp);
    bo = par.classList.contains(journalCSS.blNew);
    if(bo) par = par.parentElement;
    if (inps[inp[0].id] && inps[inp[1].id]) {
        if(bo) {
            dispatch(changeJType(undefined, inp[0].value, inp[1].value));
        } else {
            dispatch(changeJType(inp[0].id.split("_")[1], inp[0].value, inp[1].value));
        }
        par.setAttribute('data-st', '0');
    }
}

function onFinM(e) {
    let par, inp;
    par = e.target.parentElement;
    inp = par.querySelector("textarea");
    if (inp.value.length != 0) {
        addHomework(inp.id.split("_")[1], inp.value, par);
    }
}

function onDel(e) {
    let par, inp, idi;
    par = e.target.parentElement;
    inp = par.querySelectorAll("." + journalCSS.inp);
    idi = inp[0].id.split("_")[1];
    dispatch(changeJType(undefined, idi));
    parb.upddel = true;
}

function onClose(e, dbut) {
    let par = e.target.parentElement;
    if(par.classList.contains(journalCSS.blNew)) par = par.parentElement;
    if(dbut) par = document.querySelector("." + journalCSS.AppHeader);
    par.setAttribute('data-st', '0');
}

function chStatB(e) {
    let el, idp, ids;
    el = e.target;
    ids = el.id.split("_");
    idp = (ids[0] == "inpt" ? "inpv_" : "inpt_") + ids[1];
    inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
    el.dataset.mod = inps[el.id] ? '0' : '1';
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +(inps[el.id] && inps[idp]));
}

function chStatM(e) {
    let el, ids;
    el = e.target;
    ids = el.value.length != 0;
    el.dataset.mod = ids ? '0' : '1';
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +ids);
}

function chM(kid, day, per) {
    if(jourInfo.mar == 0) return;
    let st, type;
    st = jourInfo.jur.kids[kid].days[day];
    if(per != undefined) {
        if(jourInfo.mar == "Н") return;
        if(jourInfo.mar == "Л") {
            type = CHANGE_PJOURNAL_DEL_PER_MARKS;
        } else {
            type = CHANGE_PJOURNAL_PER_MARKS;
            if (st == undefined) {
                st = {mark : jourInfo.mar};
            }
        }
    } else if (st == undefined) {
        type = CHANGE_PJOURNAL_MARKS;
        st = {};
        if(jourInfo.typs[jourInfo.typ]) {
            st.weight = jourInfo.mar == "Н" ? 1 : jourInfo.typs[jourInfo.typ];
        }
        if(jourInfo.mar == "Л" || !st.weight) return;
    } else {
        if(jourInfo.typs[jourInfo.typ]) {
            st.weight = jourInfo.mar == "Н" ? 1 : jourInfo.typs[jourInfo.typ];
        } else if(jourInfo.mar == "Н" || jourInfo.typ == "") {
            st.weight = 1;
        }
        if(!st.weight) return;
        if(jourInfo.mar == "Л") {
            type = CHANGE_PJOURNAL_DEL_MARKS;
        } else {
            type = CHANGE_PJOURNAL_MARKS;
        }
    }
    if (type == CHANGE_PJOURNAL_MARKS || type == CHANGE_PJOURNAL_PER_MARKS) {
        addMark(st.weight, kid, day, per);
    } else {
        dispatch(changePjournal(type, undefined, st, kid, day, per));
    }
}

function getDate(dat) {
    let d = dat.split('.');
    return new Date("20" + [d[2], d[1], d[0]].join("-"));
}

function getDayofMonth(da) {
    let month, dat;
    if(!da) return;
    month = da.toLocaleString("ru", {month:"2-digit"});
    dat = da.toLocaleString("ru", month == pari.lMonth ? {day:"2-digit"} : {day:"2-digit", month:"short"});
    pari.lMonth = month;
    return dat;
}

function getDiff(dat, dat1) {
    let d, d1;
    d = typeof dat == "string" ? getDate(dat) : dat;
    d1 = typeof dat1 == "string" ? getDate(dat1) : dat1;
    return (d - d1) > 0;
}

function getDayS(date, dif) {
    let day, dateC;
    dateC = new Date(date.getTime());
    day = (dateC.getDay() || 7) - 1;
    if(day || dif) dateC.setDate(dateC.getDate() - day + dif);
    return dateC;
}

function getDateS(date) {
    let ret = date.toLocaleString("ru", {day:"2-digit", month: "2-digit", year:"2-digit"});
    if(lastD == date) {
        ret += "," + lastDI;
        lastDI++;
    } else {
        if(lastDI) lastDI = 0;
    }
    lastD = date;
    return ret;
}

function getDays() {
    if(!jourInfo.predms || !schedulesInfo || !jourInfo.min || !jourInfo.max || !groupsInfo.els.groups) return;
    let schOfSubj, monOfMin, week, d, les, datD, min, max, maxB, rez;
    week = 0;
    console.log("sheeeed", schedulesInfo);
    days = [];
    schOfSubj = {};
    for(d in schedulesInfo) {
        for(les in schedulesInfo[d].lessons) {
            // if(schedulesInfo[d].lessons[les].name != jourInfo.predms[jourInfo.predm]) continue;
            if(schedulesInfo[d].lessons[les].name != jourInfo.predms[jourInfo.predm] || (cState.role == 2 && schedulesInfo[d].lessons[les].group != groupsInfo.els.groups[groupsInfo.els.group])) continue;
            if(!schOfSubj[d]) schOfSubj[d] = [];
            if(!schOfSubj[d].indexOf(les) > -1) {
                schOfSubj[d].push(les);
            }
        }
    }
    console.log(schOfSubj);
    min = getDate(jourInfo.min);
    max = getDate(jourInfo.max);
    console.log(min);
    monOfMin = getDayS(min, 0);
    console.log(monOfMin);

    if(Object.getOwnPropertyNames(schOfSubj).length) {
        while (!maxB) {
            for (d in schOfSubj) {
                datD = getDayS(monOfMin, +d + 7 * week);
                maxB = getDiff(datD, max) || getDiff(datD, new Date());
                if (!(maxB || getDiff(min, datD))) {
                    for (les of schOfSubj[d]) days.push(datD);
                }
            }
            week++;
        }
    }
    rez = days.map(param =>
        <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iTextD} id={journalCSS.nav_i}>
            {getDayofMonth(param)}
        </div>
    )
    for(let i = 0; i < days.length; i++) {
        days[i] = getDateS(days[i]);
    }
    console.log("test1", days);
    return rez;
}

function selTyp(p) {
    dispatch(changePjournal(CHANGE_PJOURNAL, "typ", p));
}

function selMar(p) {
    console.log("mar", p);
    dispatch(changePjournal(CHANGE_PJOURNAL, "mar", p));
}

function getBlockInstr(p3) {
    return <div className={journalCSS.blockInstrum+" "+journalCSS.ju}>
        <div className={journalCSS.blockMarks}>
            <div className={journalCSS.marks} data-tr>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "1" ? "1" : "0"} onClick={() => selMar(1)}>
                    1
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "2" ? "1" : "0"} onClick={() => selMar(2)}>
                    2
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "3" ? "1" : "0"} onClick={() => selMar(3)}>
                    3
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "4" ? "1" : "0"} onClick={() => selMar(4)}>
                    4
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "5" ? "1" : "0"} onClick={() => selMar(5)}>
                    5
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "Н" ? "1" : "0"} onClick={() => selMar("Н")}>
                    Н
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == "Л" ? "1" : "0"} onClick={() => selMar("Л")}>
                    <img className={journalCSS.imger} src={(jourInfo.mar == "Л" ? !theme.theme_ch : theme.theme_ch) ? erad : eral} alt=""/>
                </div>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.mar == 0 ? "1" : "0"} onClick={() => selMar(0)}>
                    <br/>
                </div>
            </div>
            <div className={journalCSS.nav_i} id={journalCSS.nav_i}>
                Выбрать оценку
            </div>
        </div>
        <div className={journalCSS.blockTypes}>
            <div className={journalCSS.types+" "+journalCSS.types1}>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-st="0" data-ac="0">
                    <div className={journalCSS.field+" "+journalCSS.fi}>
                        {Object.getOwnPropertyNames(jourInfo.typs).map(param => {
                            if(param.length > p3.length) p3 = param;
                        }) && p3 + ", вес: " + jourInfo.typs[p3]}
                    </div>
                    <img className={journalCSS.imgfield+" "+journalCSS.fi} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                    <img className={journalCSS.imginp+" "+journalCSS.fi} src={no} onClick={onDel} title="Удалить тип" alt=""/>
                </div>
            </div>
            <div className={journalCSS.types} data-tr data-real>
                <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac={jourInfo.typ == "" ? "1" : "0"} onClick={() => selTyp("")}>
                    <img className={journalCSS.imger} src={(jourInfo.typ == "" ? !theme.theme_ch : theme.theme_ch) ? erad : eral} alt=""/>
                </div>
                {Object.getOwnPropertyNames(jourInfo.typs).map(param =>
                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-st="0" data-ac={jourInfo.typ == param ? "1" : "0"} onClick={() => selTyp(param)}>
                        <div className={journalCSS.field+" "+journalCSS.fi}>
                            {param}, вес: {jourInfo.typs[param]}
                        </div>
                        <div className={journalCSS.preinf+" "+journalCSS.in}>
                            Тип:
                        </div>
                        <input className={journalCSS.inp+" "+journalCSS.in} id={"inpt_" + param} onChange={chStatB} defaultValue={param} type="text" pattern="^[A-Za-zА-Яа-яЁё\s0-9]+$"/>
                        <div className={journalCSS.preinf+" "+journalCSS.in}>
                            , вес:
                        </div>
                        <input className={journalCSS.inp+" "+journalCSS.in+" "+journalCSS.mass} id={"inpv_" + param} onChange={chStatB} defaultValue={jourInfo.typs[param]} type="text" pattern="^[0-9]+$"/>
                        {ele(false, "inpt_" + param, true)}
                        {ele(false, "inpv_" + param, true)}
                        <img className={journalCSS.imginp+" yes "+journalCSS.in} src={yes} onClick={onFin} title="Подтвердить изменения" alt=""/>
                        <img className={journalCSS.imginp+" "+journalCSS.in} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                        <img className={journalCSS.imgfield+" "+journalCSS.fi} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                        <img className={journalCSS.imginp+" "+journalCSS.fi} src={no} onClick={onDel} title="Удалить тип" alt=""/>
                    </div>
                )}
                <div className={journalCSS.nav_iZag} data-st="0">
                    <div className={journalCSS.nav_i+" "+journalCSS.chPass} id={journalCSS.nav_i} data-ac='1' onClick={onEdit}>
                        Добавить новый тип
                    </div>
                    <div className={journalCSS.nav_i+" "+journalCSS.blNew} id={journalCSS.nav_i} data-ac="0">
                        <div className={journalCSS.preinf+" "+journalCSS.in}>
                            Тип:
                        </div>
                        <input className={journalCSS.inp+" "+journalCSS.in} id={"inpnt_"} onChange={chStatB} type="text" pattern="^[A-Za-zА-Яа-яЁё\s0-9]+$"/>
                        <div className={journalCSS.preinf+" "+journalCSS.in}>
                            , вес:
                        </div>
                        <input className={journalCSS.inp+" "+journalCSS.in+" "+journalCSS.mass} id={"inpnv_"} onChange={chStatB} type="text" pattern="^[0-9]+$"/>
                        {ele(false, "inpnt_", true)}
                        {ele(false, "inpnv_", true)}
                        <img className={journalCSS.imginp+" yes "+journalCSS.in} src={yes} onClick={onFin} title="Подтвердить" alt=""/>
                        <img className={journalCSS.imginp+" "+journalCSS.in} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                    </div>
                </div>
            </div>
            <div className={journalCSS.nav_i} id={journalCSS.nav_i}>
                Выбрать тип оценки
            </div>
        </div>
        <div className={journalCSS.nav_i} id={journalCSS.nav_i} onClick={e=>onEdit(e, true)}>
            Задать домашнее задание
        </div>
    </div>;
}

function onCon(e) {
    setInfoP1();
}

function setInfoP1() {
    let scr, e, marks;
    sendToServer({
        uuid: cState.uuid
    }, 'POST', cPjournal+"getInfoP1")
        .then(data => {
            console.log(data);
            if(data.error == false) {
                dispatch(changePjournal(CHANGE_PJOURNAL, "pers", data.bodyPers));
                dispatch(changeAnalytics(CHANGE_SCHEDULE_GL, 0, 0, 0, data.bodyS));
                dispatch(changePjournal(CHANGE_PJOURNAL, "predms", data.bodyPred));
                dispatch(changePjournal(CHANGE_PJOURNAL, "min", data.min));
                dispatch(changePjournal(CHANGE_PJOURNAL, "max", data.max));
                if (!data.bodyPred[jourInfo.predm]) {
                    dispatch(changePjournal(CHANGE_PJOURNAL, "predm", 0));
                    setInfoP2(data.bodyPred[0]);
                } else {
                    setInfoP2(data.bodyPred[jourInfo.predm]);
                }
                scr = document.querySelector("." + journalCSS.days);
                if(!scr) return;
                scr.scrollTo(scr.scrollWidth, 0);
                marks = document.querySelector("." + journalCSS.marks);
                if(marks) marks.addEventListener("transitionend", trEnd);
                document.querySelector("." + journalCSS.types + "[data-real]").addEventListener("transitionend", trEnd);
                for(e of document.querySelectorAll("." + journalCSS.nav_i + " > [id^='inpt_']")){
                    chStatB({target: e});
                }
                for(e of document.querySelectorAll("." + journalCSS.nav_i + " > [id^='inpv_']")){
                    chStatB({target: e});
                }
                for(e of document.querySelectorAll("." + journalCSS.nav_i + " > [id^='inpd_']")){
                    chStatM({target: e});
                }
                chStatB({target: document.querySelector("." + journalCSS.nav_i + " > [id^='inpnt_']")});
                chStatB({target: document.querySelector("." + journalCSS.nav_i + " > [id^='inpnv_']")});
            }
        });
}

function setInfoP2(predm) {
    if(!predm) return;
    sendToServer({
        uuid: cState.uuid,
        predm: predm
    }, 'POST', cPjournal+"getInfoP2")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changeGroups(CHANGE_GROUPS_GL, undefined, data.bodyG));
                if (!data.bodyG[groupsInfo.els.group]) {
                    selGr = data.firstG;
                    dispatch(changeGroups(CHANGE_GROUPS_GR, undefined, parseInt(data.firstG)));
                    setInfoP3(data.firstG);
                } else {
                    selGr = groupsInfo.els.group;
                    setInfoP3(groupsInfo.els.group);
                }
            }
        });
}

function setInfoP3(group) {
    if(!group) return;
    sendToServer({
        uuid: cState.uuid,
        group: group
    }, 'POST', cPjournal+"getInfoP3")
        .then(data => {
            console.log(data);
            if(data.error == false){
                let weight, sum, mar, wei;
                for(let kid in data.bodyK) {
                    weight = 0;
                    sum = 0;
                    for(let day in data.bodyK[kid].days) {
                        mar = parseInt(data.bodyK[kid].days[day].mark);
                        if(!mar || isNaN(mar)) continue;
                        wei = parseInt(data.bodyK[kid].days[day].weight);
                        if(!wei) wei = 1;
                        sum += mar*wei;
                        weight += wei;
                    }
                    if (sum && weight) {
                        data.bodyK[kid].avg.mark = (sum/weight).toFixed(2);
                    }
                }
                dispatch(changePjournal(CHANGE_PJOURNAL_GL, 0, data.bodyK));
                dispatch(changePjournal(CHANGE_PJOURNAL, "dz", data.bodyD));
            }
        });
}

function addMarkC(e) {
    const msg = JSON.parse(e.data);
    console.log("sdf", msg);
    if (msg.body.per) {
        dispatch(changePjournal(CHANGE_PJOURNAL_PER_MARKS, undefined, msg.body, msg.kid, undefined, msg.body.per));
    } else {
        let weight, sum, mar, wei;
        sum = parseInt(msg.body.mark);
        if (sum && !isNaN(sum)) {
            weight = parseInt(msg.body.weight);
            if (!weight) weight = 1;
            sum *= weight;
            for (let day in jourInfo.jur[msg.kid].days) {
                mar = parseInt(jourInfo.jur[msg.kid].days[day].mark);
                if (!mar || isNaN(mar)) continue;
                wei = parseInt(jourInfo.jur[msg.kid].days[day].weight);
                if (!wei) wei = 1;
                sum += mar * wei;
                weight += wei;
            }
            if (sum && weight) {
                // data.bodyK[kid].avg.mark = (sum/weight).toFixed(2);
                mar = {mark: (sum / weight).toFixed(2)};
                dispatch(changePjournal(CHANGE_PJOURNAL_PER_MARKS, undefined, mar, msg.kid, undefined, "mark"));
            }
        }
        dispatch(changePjournal(CHANGE_PJOURNAL_MARKS, undefined, msg.body, msg.kid, msg.day));
    }
}

function addMark (weight, kid, day, per) {
    console.log("addMark");
    sendToServer({
        uuid: cState.uuid,
        weight: weight,
        kid: kid,
        day: day,
        mark: jourInfo.mar,
        style: jourInfo.typ,
        group: groupsInfo.els.group,
        per: per
    }, 'POST', cPjournal+"addMark")
}

function addHomework (day, homework, par) {
    console.log("addHomework");
    sendToServer({
        uuid: cState.uuid,
        day: day,
        group: groupsInfo.els.group,
        homework: homework
    }, 'POST', cPjournal+"addHomework")
        .then(data => {
            if(data.error == false) {
                par.setAttribute('data-st', '0');
            }
        });
}

function addHomeworkC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeDZ(msg.day, msg.homework));
}

export function Journal() {
    theme = useSelector(themes);
    cState = useSelector(states);
    schedulesInfo = useSelector(schedules);
    jourInfo = useSelector(pjournal);
    groupsInfo = useSelector(groups);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Journal.jsx");
        if(eventSource.readyState == EventSource.OPEN) setInfoP1();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addMarkC', addMarkC, false);
        eventSource.addEventListener('addHomeworkC', addHomeworkC, false);
        setActived(9);
        dispatch(changeEvents(CHANGE_EVENTS_RL, false));
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch(changeEvents(CHANGE_EVENTS_RL, true));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addMarkC', addMarkC);
            eventSource.removeEventListener('addHomeworkC', addHomeworkC);
            console.log("I was triggered during componentWillUnmount Journal.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(parb.upddel) {
            parb.upddel = false;
            let idi;
            for(let el of document.querySelectorAll("." + journalCSS.inp)){
                idi = el.id.split("_");
                if(idi[1] != "") el.value = idi[0] == "inpt" ? idi[1] : jourInfo.typs[idi[1]];
            }
        }
        if(groupsInfo.els.group && selGr != groupsInfo.els.group && eventSource.readyState == EventSource.OPEN) {
            setInfoP2(jourInfo.predms[jourInfo.predm]);
        }
        console.log('componentDidUpdate Journal.jsx');
    });
    let p3, kidsK, predmsK, pers;
    p3 = "";
    if(jourInfo.jur) kidsK = Object.getOwnPropertyNames(jourInfo.jur);
    if(jourInfo.predms) predmsK = Object.getOwnPropertyNames(jourInfo.predms);
    if(jourInfo.pers) pers = Object.getOwnPropertyNames(jourInfo.pers);
    console.log("test", kidsK, predmsK);
    return <div className={journalCSS.AppHeader} data-st='0'>
        <Helmet>
            <title>Журнал</title>
        </Helmet>
        {!kidsK || !predmsK || !kidsK.length || !predmsK.length ?
                <ErrFound text={errText}/>
            :
                <>
                    <nav className={journalCSS.panel} id="her">
                        <div className={journalCSS.pane}>
                            <Pane cla={true}/>
                        </div>
                        {getPredms(predmsK)}
                    </nav>
                    <div className={journalCSS.blockPredm+" "+journalCSS.ju}>
                        <div className={journalCSS.predm}>
                            <div className={journalCSS.days}>
                                <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.namd} id={journalCSS.nav_i}>
                                    <br/>
                                </div>
                                <div className={journalCSS.daysGrid}>
                                    <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iBr} id={journalCSS.nav_i}>
                                        <br/>
                                    </div>
                                    {ele(0, "lMonth")}
                                    {getDays()}
                                    <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur}>
                                        <div className={journalCSS.nav_iText}>
                                            Средняя
                                        </div>
                                    </div>
                                    {jourInfo.pers && pers.map((param, i, x, per = jourInfo.pers[param]) =>
                                        <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur}>
                                            <div className={journalCSS.nav_iTextPer} data-s={per.length > 2 ? 1 : 0}>
                                                {per}
                                            </div>
                                        </div>
                                    )}
                                </div>
                                {kidsK && kidsK.map((param, i, x, kid = jourInfo.jur[param]) => <>
                                    <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" nam " + journalCSS.nam} id={journalCSS.nav_i}>
                                        {kid.name}
                                    </div>
                                    <div className={journalCSS.predmGrid} id={param}>
                                        <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iBr} id={journalCSS.nav_i}>
                                            <br/>
                                        </div>
                                        {days.map((param1, i1, x1, marL = kid.days ? kid.days[param1] : undefined) => <>
                                            {!marL && <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur} id={journalCSS.nav_i} onClick={e=>chM(param, param1)}>
                                                <br/>
                                            </div>}
                                            {marL && <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur} id={journalCSS.nav_i} onClick={e=>chM(param, param1)}>
                                                {marL.mark}
                                                {marL.weight > 1 && <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iWeight} id={journalCSS.nav_i}>
                                                    {marL.weight}
                                                </div>}
                                            </div>}
                                        </>)}
                                        <div className={journalCSS.nav_i + ' ' + journalCSS.nav_iJur + " " + journalCSS.nav_iTextM} style={{fontSize: "0.85vw"}}>
                                            {kid.avg ? kid.avg.mark : <br/>}
                                        </div>
                                        {jourInfo.pers && pers.map(param1 =>
                                            <div className={journalCSS.nav_i + ' ' + journalCSS.nav_iJur + " " + journalCSS.nav_iTextM} onClick={() => chM(param, undefined, param1)}>
                                                {kid.avg && kid.avg[param1] ? kid.avg[param1] : <br/>}
                                            </div>
                                        )}
                                    </div>
                                </>)}
                            </div>
                        </div>
                    </div>
                    {getBlockInstr(p3)}
                    <div className={journalCSS.blockDom+" "+journalCSS.dom}>
                        <div className={journalCSS.day}>
                            <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i}>
                                Дата
                            </div>
                            <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i}>
                                Домашнее задание
                            </div>
                            {days && days.reverse().map((param, i, x, dM = param.split(",")[0]) => <>
                                <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i}>
                                    {dM}
                                </div>
                                <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i} data-st="0">
                                    <pre className={journalCSS.field+" "+journalCSS.fi}>
                                        {jourInfo.dz[param] || <br/>}
                                    </pre>
                                    <textarea className={journalCSS.inp+" "+journalCSS.in+" "+journalCSS.inparea} id={"inpd_" + param} defaultValue={jourInfo.dz[param]} onChange={chStatM}/>
                                    <img className={journalCSS.imginp+" yes "+journalCSS.in} src={yes} onClick={onFinM} title="Подтвердить изменения" alt=""/>
                                    <img className={journalCSS.imginp+" "+journalCSS.in} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                    <img className={journalCSS.imgfield+" "+journalCSS.fi} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                </div>
                            </>)}
                        </div>
                    </div>
                    <div className={journalCSS.blockInstrum+" "+journalCSS.dom}>
                        <div className={journalCSS.nav_i} id={journalCSS.nav_i} onClick={e=>onClose(e, true)}>
                            Вернуться к журналу
                        </div>
                    </div>
                </>
        }
    </div>
}
export default Journal;