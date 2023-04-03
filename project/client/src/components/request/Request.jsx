import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import requestCSS from './request.module.css';
import {useDispatch, useSelector} from "react-redux";
import yes from "../../media/yes.png";
import no from "../../media/no.png";
import ed from "../../media/edit.png";
import ErrFound from "../other/error/ErrFound";
import {requests, states} from "../../store/selector";
import {
    CHANGE_EVENTS_CLEAR,
    CHANGE_REQUEST,
    CHANGE_REQUEST_DEL,
    CHANGE_REQUEST_GL,
    CHANGE_REQUEST_PARAM,
    changeEvents,
    changeReq
} from "../../store/actions";
import {eventSource, send, setActived} from "../main/Main";

let dispatch, requestInfo, inps, cState, errText;
inps = {inpntt : "Текст", inpnzt : "Заголовок", inpndt: new Date().toLocaleString("ru", {day:"2-digit", month: "2-digit", year:"numeric"})};
errText = "Заявок нет...";

let [_, forceUpdate] = [];

function getEdField(edFi, titleEd, x, inf, inp, info, placeholder, pattern) {
    return (<>
        <div className={requestCSS.fi}>
            {edFi}
            <img className={requestCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
        </div>
        <div className={requestCSS.ed}>
            <div className={requestCSS.preinf}>
                {titleEd}
            </div>
            {edFi.type == "pre" ?
                <textarea className={requestCSS.inp+" "+requestCSS.inparea} id={inp} placeholder={placeholder} defaultValue={inf} data-id={x} onChange={chStatB}/>
                :
                <input className={requestCSS.inp} id={inp} placeholder={placeholder} pattern={pattern} defaultValue={inf} data-id={x} onChange={chStatB}/>
            }
            {ele(false, inp)}
            <img className={requestCSS.imginp+" yes "} src={yes} onClick={onFin} title="Подтвердить" alt=""/>
            <img className={requestCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
        </div>
    </>)
}

function getAdd(info, x) {
    let edFi, dat, datFi, zag, zagFi, tex, texFi;
    zag = info[x].title;
    zagFi = <h2 className={requestCSS.zag}>
        {zag}
    </h2>;
    dat = info[x].date;
    datFi = <span className={requestCSS.date}>
        {dat}
    </span>;
    tex = info[x].text;
    texFi = <pre className={requestCSS.field}>
        {tex}
    </pre>;
    edFi = (
        <div className={requestCSS.ns}>
            <div className={requestCSS.za} data-st="0">
                {getEdField(zagFi, "Заголовок:", x, zag, "inpnzt_" + (x?x:""), info)}
            </div>
            <div className={requestCSS.da} data-st="0">
                {getEdField(datFi, "Дата:", x, dat, "inpndt_" + (x?x:""), info, "ДД.ММ.ГГГГ", "^[0-9.]+$")}
            </div>
            <div className={requestCSS.te} data-st="0">
                {getEdField(texFi, "Текст:", x, tex, "inpntt_" + (x?x:""), info)}
            </div>
            <div className={requestCSS.upr} data-id={x}>
                <img className={requestCSS.imginp+" "} style={{marginRight: "1vw"}} src={no} onClick={onDel} title="Удалить заявку" alt=""/>
            </div>
        </div>
    );
    return edFi
}

function onEdit(e) {
    let par;
    par = e.target.parentElement;
    if(par.classList.contains(requestCSS.line)){
        par.setAttribute('data-st', '1');
    }
    if(par.parentElement.classList.contains(requestCSS.te) || par.parentElement.classList.contains(requestCSS.da) || par.parentElement.classList.contains(requestCSS.za)){
        par = par.parentElement;
        par.setAttribute('data-st', '1');
    }
    if(par.parentElement.parentElement.classList.contains(requestCSS.im)){
        par = par.parentElement.parentElement;
        par.setAttribute('data-st', '1');
    }
}

function onFin(e) {
    let par, inp, bul;
    par = e.target.parentElement;
    bul = par.parentElement.classList.contains(requestCSS.te);
    inp = par.querySelector(bul ? "textarea" : "input");
    if (inps[inp.id]) {
        inp.setAttribute("data-mod", '0');
        if(bul) {
            par = par.parentElement;
            send({
                login: cState.login,
                id: inp.getAttribute("data-id"),
                text: inp.value
            }, 'POST', "requests", "chText")
                .then(data => {
                    if(data.error == false){
                        dispatch(changeReq(CHANGE_REQUEST_PARAM, inp.getAttribute("data-id"), inp.value,"text"));
                    }
                });
        }
        if(par.parentElement.classList.contains(requestCSS.da)){
            par = par.parentElement;
            send({
                login: cState.login,
                id: inp.getAttribute("data-id"),
                date: inp.value
            }, 'POST', "requests", "chDate")
                .then(data => {
                    if(data.error == false){
                        dispatch(changeReq(CHANGE_REQUEST_PARAM, inp.getAttribute("data-id"), inp.value,"date"));
                    }
                });
        }
        if(par.parentElement.classList.contains(requestCSS.za)){
            par = par.parentElement;
            send({
                login: cState.login,
                id: inp.getAttribute("data-id"),
                title: inp.value
            }, 'POST', "requests", "chTitle")
                .then(data => {
                    if(data.error == false){
                        dispatch(changeReq(CHANGE_REQUEST_PARAM, inp.getAttribute("data-id"), inp.value,"title"));
                    }
                });
        }
        par.setAttribute('data-st', '0');
    } else {
        inp.setAttribute("data-mod", '1');
    }
}

function onDel(e) {
    let par = e.target.parentElement;
    if(par.classList.contains(requestCSS.upr)){
        if (par.hasAttribute("data-id")) {
            send({
                login: cState.login,
                id: par.getAttribute("data-id")
            }, 'POST', "requests", "delReq")
                .then(data => {
                    if(data.error == false){
                        dispatch(changeReq(CHANGE_REQUEST_DEL, par.getAttribute("data-id")));
                    }
                });
        }
    }
}

function onClose(e) {
    let par = e.target.parentElement;
    if(par.parentElement.classList.contains(requestCSS.te) || par.parentElement.classList.contains(requestCSS.da) || par.parentElement.classList.contains(requestCSS.za)){
        par = par.parentElement;
        par.setAttribute('data-st', '0');
    }
    if(par.classList.contains(requestCSS.upr)){
        par = par.parentElement.parentElement;
        par.setAttribute('data-st', '0');
    }
}

function setInfo() {
    send({
        type: "REQUESTS",
        uuid: cState.uuid
    }, 'POST', "auth", "infCon");
    send({
        login: cState.login
    }, 'POST', "requests", "getRequests")
        .then(data => {
            if(data.error == false){
                dispatch(changeReq(CHANGE_REQUEST_GL, undefined, data.body));
                for(let el of document.querySelectorAll("." + requestCSS.ed + " > *[id^='inpn']")){
                    chStatB({target: el});
                }
            }
        });
}

function addReq(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeReq(CHANGE_REQUEST, msg.id, msg.body));
}

function chText(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeReq(CHANGE_REQUEST_PARAM, msg.id, msg.text,"text"));
}

function chDate(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeReq(CHANGE_REQUEST_PARAM, msg.id, msg.date,"date"));
}

function chTitle(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeReq(CHANGE_REQUEST_PARAM, msg.id, msg.title,"title"));
}

function delReq(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeReq(CHANGE_REQUEST_DEL, msg.id));
}

function onCon(e) {
    setInfo();
}

function chStatB(e) {
    let el = e.target;
    inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
    el.setAttribute("data-mod", inps[el.id] ? '0' : '1');
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +inps[el.id]);
}

function ele (x, par) {
    if(!inps[par]) inps[par] = x;
}

export function Request() {
    requestInfo = useSelector(requests);
    cState = useSelector(states);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Request.jsx");
        setActived(11);
        setInfo();
        eventSource.addEventListener('addReq', addReq, false);
        eventSource.addEventListener('chText', chText, false);
        eventSource.addEventListener('chDate', chDate, false);
        eventSource.addEventListener('chTitle', chTitle, false);
        eventSource.addEventListener('delReq', delReq, false);
        eventSource.addEventListener('connect', onCon, false);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('addReq', addReq);
            eventSource.removeEventListener('chText', chText);
            eventSource.removeEventListener('chDate', chDate);
            eventSource.removeEventListener('chTitle', chTitle);
            eventSource.removeEventListener('delReq', delReq);
            eventSource.removeEventListener('connect', onCon);
            console.log("I was triggered during componentWillUnmount Request.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Request.jsx');
    });
    return (
        <div className={requestCSS.header}>
            <Helmet>
                <title>Заявки</title>
            </Helmet>
            {Object.getOwnPropertyNames(requestInfo).length == 0 ?
                    <ErrFound text={errText}/>
                :
                    <div className={requestCSS.block}>
                        <section className={requestCSS.center_colum}>
                            {Object.getOwnPropertyNames(requestInfo).reverse().map(param =>
                                <div className={requestCSS.line} data-st="1" key={param}>
                                    {getAdd(requestInfo, param)}
                                </div>
                            )}
                        </section>
                    </div>
            }
        </div>
    )
}
export default Request;