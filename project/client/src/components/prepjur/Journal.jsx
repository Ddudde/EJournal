import React, {useEffect, useRef} from "react";
import journalCSS from './journal.module.css';
import {Helmet} from "react-helmet-async";
import {useDispatch, useSelector} from "react-redux";
import {pjournal, states, themes} from "../../store/selector";
import {setActived} from "../main/Main";
import mapd from "../../media/Map_symbolD.png";
import mapl from "../../media/Map_symbolL.png";
import {changeDZ, changeJType, changePjournal, changePjournalMarks} from "../../store/actions";
import warn from "../../media/warn_big.png";
import erad from "../../media/eraserd.png";
import eral from "../../media/eraserl.png";
import no from "../../media/no.png";
import ed from "../../media/edit.png";
import yes from "../../media/yes.png";
import Pane from "../other/pane/Pane";

let jourInfo, dispatch, theme, lma, lmal, pari, parb, inps, lty, ltyl;
pari = {elems1: 0, maxEl: 0, lMonth: 0};
parb = {upddel: false, updnew: false, lscr: false};
inps = {};

function getPredms() {
    pari.elems++;
    return (
        <div className={journalCSS.predBlock}>
            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+' '+journalCSS.predEl} id={journalCSS.nav_i}>
                <div className={journalCSS.predInf}>Предмет:</div>
                <div className={journalCSS.predText}>{jourInfo.predms[jourInfo.predm]}</div>
                <img className={journalCSS.mapImg} src={theme.theme_ch ? mapd : mapl} alt=""/>
            </div>
            <div className={journalCSS.predMenu}>
                {jourInfo.predms && Object.getOwnPropertyNames(jourInfo.predms).map(param1 =>
                    <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+' '+journalCSS.pred} id={journalCSS.nav_i} onClick={() => (dispatch(changePjournal("predm", param1)))}>
                        <div className={journalCSS.predInf}>Предмет:</div>
                        <div className={journalCSS.predText}>{jourInfo.predms[param1]}</div>
                    </div>
                )}
            </div>
        </div>
    )
}

function getDate(dat) {
    let d = dat.split('.');
    return new Date("20" + [d[2], d[1], d[0]].join("-"));
}

function getDay(da) {
    let date = getDate(da), month = date.toLocaleString("ru", {month:"2-digit"}), dat = date.toLocaleString("ru", month == pari.lMonth ? {day:"2-digit"} : {day:"2-digit", month:"short"});
    pari.lMonth = month;
    return dat;
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

function elex(days) {
    let d = Object.getOwnPropertyNames(days)[Object.getOwnPropertyNames(days).length-1];
    return d ? d : -1;
}

function onEdit(e) {
    let par = e.target.parentElement;
    if(e.target.classList.contains(journalCSS.dbut)) par = document.querySelector("." + journalCSS.AppHeader);
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
            parb.updnew = true;
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
    inp = par.querySelector("." + journalCSS.inp);
    if (inp.value.length != 0) {
        dispatch(changeDZ(inp.id.split("_")[1], inp.value));
        par.setAttribute('data-st', '0');
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

function onClose(e) {
    let par = e.target.parentElement;
    if(par.classList.contains(journalCSS.blNew)) par = par.parentElement;
    if(e.target.classList.contains(journalCSS.dbut)) par = document.querySelector("." + journalCSS.AppHeader);
    par.setAttribute('data-st', '0');
}

function chStatB(e) {
    let el, idp, ids;
    el = e.target;
    ids = el.id.split("_");
    idp = (ids[0] == "inpt" ? "inpv_" : "inpt_") + ids[1];
    inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
    if (inps[el.id]) {
        el.style.outline = "none black";
    } else {
        el.style.animation = "but 1s ease infinite";
        setTimeout(function () {
            el.style.animation = "none"
        }, 1000);
        el.style.outline = "solid red";
    }
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +(inps[el.id] && inps[idp]));
}

function chStatM(e) {
    let el, ids;
    el = e.target;
    ids = el.value.length != 0;
    if (ids) {
        el.style.outline = "none black";
    } else {
        el.style.animation = "but 1s ease infinite";
        setTimeout(function () {
            el.style.animation = "none"
        }, 1000);
        el.style.outline = "solid red";
    }
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +ids);
}

function chM(kid, day, per) {
    dispatch(changePjournalMarks(kid, day, jourInfo.mar, jourInfo.jur.kids[kid].days[day], per, jourInfo.typ, jourInfo.typs[jourInfo.typ]));
}

function cli() {
    lma.setAttribute("data-ac", 0);
    this.setAttribute("data-ac", 1);
    lma = this;
}

function cli1() {
    lty.setAttribute("data-ac", 0);
    this.setAttribute("data-ac", 1);
    lty = this;
}

export function Journal() {
    const cState = useSelector(states);
    theme = useSelector(themes);
    jourInfo = useSelector(pjournal);
    pari.maxEl = Object.getOwnPropertyNames(jourInfo.jur.day).length;
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Journal.jsx");
        lty = ltyl;
        let scr = document.querySelector("." + journalCSS.days);
        scr.scrollTo(scr.scrollWidth, 0);
        document.querySelector("." + journalCSS.marks).addEventListener("transitionend", trEnd);
        document.querySelector("." + journalCSS.types + "[data-real]").addEventListener("transitionend", trEnd);
        for(let e of document.querySelectorAll("." + journalCSS.marks + " > ." + journalCSS.nav_i)){
            e.addEventListener("click", cli);
        }
        setActived(9);
        for(let e of document.querySelectorAll("." + journalCSS.types + "[data-real] > ." + journalCSS.nav_i)){
            e.addEventListener("click", cli1);
        }
        for(let e of document.querySelectorAll("." + journalCSS.nav_i + " > [id^='inpt_']")){
            chStatB({target: e});
        }
        for(let e of document.querySelectorAll("." + journalCSS.nav_i + " > [id^='inpv_']")){
            chStatB({target: e});
        }
        for(let e of document.querySelectorAll("." + journalCSS.nav_i + " > [id^='inpd_']")){
            chStatM({target: e});
        }
        chStatB({target: document.querySelector("." + journalCSS.nav_i + " > [id^='inpnt_']")});
        chStatB({target: document.querySelector("." + journalCSS.nav_i + " > [id^='inpnv_']")});
        return function() {
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount Journal.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(parb.updf){
            parb.updf = false;
            console.log('componentDidUpdate onlyRender Journal.jsx');
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
        if(parb.updnew) {
            parb.updnew = false;
            let ty = Object.getOwnPropertyNames(jourInfo.typs);
            ty = ty[ty.length-1];
            document.querySelector("#inpt_" + ty).parentElement.addEventListener("click", cli1);
        }
        console.log('componentDidUpdate Journal.jsx');
    });
    let p3 = "";
    return (
        <>
            <Helmet>
                <title>Журнал</title>
            </Helmet>
            <div className={journalCSS.AppHeader} data-st='0'>
                {Object.getOwnPropertyNames(jourInfo.jur.kids).length == 0 ?
                    <div className={journalCSS.block}>
                        <img alt="banner" src={warn}/>
                        <div className={journalCSS.block_text}>
                            К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить
                            информацию.
                        </div>
                    </div> :
                    <>
                        <nav className={journalCSS.panel} id="her">
                            {ele(0, "elems")}
                            <div style={{width:"100%", height: "100%"}}>
                                <Pane cla={true}/>
                            </div>
                            {getPredms()}
                        </nav>
                        <div className={journalCSS.blockPredm+" "+journalCSS.ju}>
                            <div className={journalCSS.predm}>
                                <div className={journalCSS.days}>
                                    <div className={journalCSS.daysGrid} style={{gridTemplate: "15vh /20vw repeat(" + (pari.maxEl + jourInfo.pers.length + 1) + ", 2vw)"}}>
                                        <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.namd} id={journalCSS.nav_i}>
                                            <br/>
                                        </div>
                                        <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iBr} id={journalCSS.nav_i}>
                                            <br/>
                                        </div>
                                        {ele(0, "lMonth")}
                                        {Object.getOwnPropertyNames(jourInfo.jur.day).map(param =>
                                            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iTextD} id={journalCSS.nav_i}>
                                                {getDay(jourInfo.jur.day[param])}
                                            </div>
                                        )}
                                        <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur}>
                                            <div className={journalCSS.nav_iText}>
                                                Средняя
                                            </div>
                                        </div>
                                        {jourInfo.pers.map(param =>
                                            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur}>
                                                <div className={journalCSS.nav_iTextPer} data-s={param.length > 2 ? 1 : 0}>
                                                    {param}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                    {Object.getOwnPropertyNames(jourInfo.jur.kids).map(param =>
                                        <div className={journalCSS.predmGrid} style={{gridTemplate: "5vh /20vw repeat(" + (pari.maxEl + jourInfo.pers.length + 1) + ", 2vw)"}} id={param}>
                                            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" nam " + journalCSS.nam} id={journalCSS.nav_i}>
                                                {param}
                                            </div>
                                            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iBr} id={journalCSS.nav_i}>
                                                <br/>
                                            </div>
                                            {ele(0, "elems1")}
                                            {Object.getOwnPropertyNames(jourInfo.jur.kids[param].days).map(param1 => <>
                                                    {parseInt(param1) - pari.elems1 > 0 && Array(parseInt(param1) - pari.elems1).fill('').map((param2, p, o, ele = pari.elems1) =>
                                                        <>
                                                            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur} id={journalCSS.nav_i} onClick={()=>(chM(param, p + ele))}>
                                                                <br/>
                                                            </div>
                                                        </>
                                                    )}
                                                    {ele(parseInt(param1)+1, "elems1")}
                                                    <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur} id={journalCSS.nav_i} onClick={()=>(chM(param, param1))}>
                                                        {jourInfo.jur.kids[param].days[param1].mark}
                                                        {jourInfo.jur.kids[param].days[param1].weight > 1 && (<div className={journalCSS.nav_i+' '+journalCSS.nav_iJur+" "+journalCSS.nav_iWeight} id={journalCSS.nav_i}>
                                                            {jourInfo.jur.kids[param].days[param1].weight}
                                                        </div>)}
                                                    </div>
                                                </>
                                            )}
                                            {elex(jourInfo.jur.kids[param].days) < pari.maxEl-1 && Array(pari.maxEl-1-elex(jourInfo.jur.kids[param].days)).fill('').map((param3, p) =>
                                                <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur} id={journalCSS.nav_i} onClick={()=>(chM(param, p+1+parseInt(elex(jourInfo.jur.kids[param].days))))}>
                                                    <br/>
                                                </div>
                                            )}
                                            <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur + " " + journalCSS.nav_iTextM} style={{fontSize:"0.85vw"}}>
                                                {jourInfo.jur.kids[param].avg.mark}
                                            </div>
                                            {jourInfo.pers.map(param1 =>
                                                <div className={journalCSS.nav_i+' '+journalCSS.nav_iJur + " " + journalCSS.nav_iTextM} onClick={()=>(chM(param, undefined, param1))}>
                                                    {jourInfo.jur.kids[param].avg[param1] ? jourInfo.jur.kids[param].avg[param1] : <br/>}
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                        <div className={journalCSS.blockInstrum+" "+journalCSS.ju}>
                            <div className={journalCSS.blockMarks}>
                                <div className={journalCSS.marks} data-tr>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", 1))}>
                                        1
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", 2))}>
                                        2
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", 3))}>
                                        3
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", 4))}>
                                        4
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", 5))}>
                                        5
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", "Н"))}>
                                        Н
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="0" onClick={() => dispatch(changePjournal("mar", "Л"))} ref={(ref)=>( !lmal && (lmal = ref))}>
                                        <img className={journalCSS.imger} src={(lmal?.getAttribute("data-ac") == "1" ? !theme.theme_ch : theme.theme_ch) ? erad : eral} alt=""/>
                                    </div>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="1" onClick={() => dispatch(changePjournal("mar", 0))} ref={(ref)=>( !lma && (lma = ref))}>
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
                                            }) && p3 + ", вес: " + jourInfo.typs[p3]
                                            }
                                        </div>
                                        <img className={journalCSS.imgfield+" "+journalCSS.fi} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                        <img className={journalCSS.imginp+" "+journalCSS.fi} src={no} onClick={onDel} title="Удалить тип" alt=""/>
                                    </div>
                                </div>
                                <div className={journalCSS.types} data-tr data-real>
                                    <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-ac="1" onClick={() => dispatch(changePjournal("typ", ""))} ref={(ref)=>( !ltyl && (ltyl = ref))}>
                                        <img className={journalCSS.imger} src={((ltyl ? ltyl.getAttribute("data-ac") == "1" : true) ? !theme.theme_ch : theme.theme_ch) ? erad : eral} alt=""/>
                                    </div>
                                    {Object.getOwnPropertyNames(jourInfo.typs).map(param =>
                                        <div className={journalCSS.nav_i} id={journalCSS.nav_i} data-st="0" data-ac="0" onClick={() => dispatch(changePjournal("typ", param))}>
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
                            <div className={journalCSS.nav_i+" "+journalCSS.dbut} id={journalCSS.nav_i} onClick={onEdit}>
                                Задать домашнее задание
                            </div>
                        </div>
                        <div className={journalCSS.blockDom+" "+journalCSS.dom}>
                            <div className={journalCSS.day}>
                                <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i}>
                                    Дата
                                </div>
                                <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i}>
                                    Домашнее задание
                                </div>
                                {Object.getOwnPropertyNames(jourInfo.jur.day).reverse().map(param =>
                                    <>
                                        <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i}>
                                            {jourInfo.jur.day[param]}
                                        </div>
                                        <div className={journalCSS.nav_i+" "+journalCSS.nav_iJur} id={journalCSS.nav_i} data-st="0">
                                            <pre className={journalCSS.field+" "+journalCSS.fi}>
                                                {jourInfo.dz[param] ? jourInfo.dz[param] : <br/>}
                                            </pre>
                                            <textarea className={journalCSS.inp+" "+journalCSS.in+" "+journalCSS.inparea} id={"inpd_" + param} defaultValue={jourInfo.dz[param]} onChange={chStatM}/>
                                            <img className={journalCSS.imginp+" yes "+journalCSS.in} src={yes} onClick={onFinM} title="Подтвердить изменения" alt=""/>
                                            <img className={journalCSS.imginp+" "+journalCSS.in} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                            <img className={journalCSS.imgfield+" "+journalCSS.fi} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>
                        <div className={journalCSS.blockInstrum+" "+journalCSS.dom}>
                            <div className={journalCSS.nav_i+" "+journalCSS.dbut} id={journalCSS.nav_i} onClick={onClose}>
                                Вернуться к журналу
                            </div>
                        </div>
                    </>
                }
            </div>
        </>
    )
}
export default Journal;