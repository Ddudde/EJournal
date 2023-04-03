import React, {useEffect, useRef} from "react";
import contactCSS from './contactMain.module.css';
import {Outlet} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {states} from "../../store/selector";
import Pane from "../other/pane/Pane";
import {eventSource, send, setActived} from "../main/Main";
import {CHANGE_CONTACT_GL, CHANGE_CONTACT_PARAM, changeContacts} from "../../store/actions";
import ed from "../../media/edit.png";
import yes from "../../media/yes.png";
import no from "../../media/no.png";

let gr, cState, dispatch, type;
type = "Por";

gr = {
    group: 0
}

export function getEdField(edFi, titleEd, inf, inp, info, inps, forceUpdate, placeholder, pattern) {
    return (<>
        <div className={contactCSS.fi}>
            {edFi}
            {titleEd != "Ссылка:" && <img className={contactCSS.imgfield} src={ed} onClick={(e)=>onEdit(e, inps, info)} title="Редактировать" alt=""/>}
        </div>
        <div className={contactCSS.ed}>
            <div className={contactCSS.preinf}>
                {titleEd}
            </div>
            {edFi.type == "pre" ?
                    <textarea className={contactCSS.inp+" "+contactCSS.inparea} id={inp} placeholder={placeholder} defaultValue={inf} onChange={(e)=>chStatB(e, inps)}/>
                :
                    <input className={contactCSS.inp} id={inp} placeholder={placeholder} pattern={pattern} defaultValue={inf} onChange={(e)=>chStatB(e, inps)}/>
            }
            {ele(false, inp, inps)}
            <img className={contactCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps)} title="Подтвердить" alt=""/>
            <img className={contactCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onClose(e, inps, forceUpdate)} title="Отменить изменения и выйти из режима редактирования" alt=""/>
        </div>
    </>)
}

export function getEdCon(info, inps, forceUpdate) {
    let telFi, tel, telM, telMFi, im, imFi;
    tel = info[type].contact;
    telFi = <pre className={contactCSS.field}>
        {tel}
    </pre>;
    telM = info[type].mapPr ? info[type].mapPr.text : undefined;
    telMFi = <pre className={contactCSS.field}>
        {telM}
    </pre>;
    im = inps.edAddIm;
    imFi = <div className={contactCSS.banner}>
        <div>
            <div>
                Изображение
            </div>
            <img className={contactCSS.imgfield} src={ed} onClick={(e)=>onEdit(e, inps, info)} title="Редактировать" alt=""/>
        </div>
    </div>;
    return (
        <section className={contactCSS.center_colum}>
            <div className={contactCSS.blockTel}>
                <h1 className={contactCSS.zag}>Телефоны для связи</h1>
                <div className={contactCSS.te} data-st="0">
                    {getEdField(telFi, "Текст:", tel, "inpntt_c", info, inps, forceUpdate)}
                </div>
            </div>
            <div className={contactCSS.map+" "+contactCSS.blockTel}>
                <h1 className={contactCSS.zag}>Карта проезда</h1>
                <div className={contactCSS.te+" mapt"} data-st="0">
                    {getEdField(telMFi, "Текст:", telM, "inpntt_m", info, inps, forceUpdate)}
                    {info[type].mapPr && info[type].mapPr.imgUrl ?
                            <span className={contactCSS.banner}>
                                <img alt="banner" src={info[type].mapPr.imgUrl} onError={errLoadAddIm}/>
                                <div className={contactCSS.upr}>
                                    <img className={contactCSS.imgfield} src={ed} onClick={(e)=>onEdit(e, inps, info)} title="Редактировать" alt=""/>
                                    <img className={contactCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onDel} title="Удалить изображение" alt=""/>
                                </div>
                            </span>
                        :
                            <div className={contactCSS.im} data-st={inps.edAddIm ? "1" : "0"}>
                                {getEdField(imFi, "Ссылка:", im, "inpnit_m", info, inps, forceUpdate, "/media/tuman.jpg")}
                            </div>
                    }
                </div>
            </div>
        </section>
    );
}

export function errorLoad(e) {
    e.target.style.display = 'none';
}

export function errLoadAddIm(e) {
    chContact("", "mapPr", "imgUrl");
    // dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, "", "mapPr", "imgUrl"));
}

export function onDel(e) {
    let par;
    par = e.target.parentElement.parentElement;
    if(par.classList.contains(contactCSS.banner)){
        chContact("", "mapPr", "imgUrl");
        // dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, "", "mapPr", "imgUrl"));
    }
}

export function onEdit(e, inps, info) {
    let par;
    par = e.target.parentElement;
    if(par.parentElement.classList.contains(contactCSS.im) || par.parentElement.classList.contains(contactCSS.te)){
        par = par.parentElement;
        par.setAttribute('data-st', '1');
    }
    if(par.parentElement.parentElement.parentElement.classList.contains(contactCSS.im)){
        par = par.parentElement.parentElement.parentElement;
        par.setAttribute('data-st', '1');
    }
    if(par.classList.contains(contactCSS.upr)){
        inps.edAddIm = info[type].mapPr.imgUrl;
        chContact("", "mapPr", "imgUrl");
        // dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, "", "mapPr", "imgUrl"));
    }
}

export function onFin(e, inps) {
    let par, inp, bul;
    par = e.target.parentElement;
    bul = par.parentElement.classList.contains(contactCSS.te);
    inp = par.querySelector(bul ? "textarea" : "input");
    if (inps[inp.id]) {
        inp.setAttribute("data-mod", '0');
        if(par.parentElement.classList.contains(contactCSS.im)) {
            if (inps.edAddIm) inps.edAddIm = undefined;
            chContact(inp.value, "mapPr", "imgUrl");
            // dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, inp.value, "mapPr", "imgUrl"));
        }
        if(bul) {
            par = par.parentElement;
            if(par.classList.contains("mapt")){
                chContact(inp.value, "mapPr", "text");
                // dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, inp.value, "mapPr", "text"));
            } else {
                chContact(inp.value, "contact");
                // dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, inp.value, "contact"));
            }
        }
        par.setAttribute('data-st', '0');
    } else {
        inp.setAttribute("data-mod", '1');
    }
}

export function onClose(e, inps, forceUpdate) {
    let par = e.target.parentElement;
    if(par.parentElement.classList.contains(contactCSS.im) || par.parentElement.classList.contains(contactCSS.te)){
        par = par.parentElement;
        if(inps.edAddIm) {
            inps.addIm = inps.edAddIm;
            inps.edAddIm = undefined;
            forceUpdate();
        } else {
            par.setAttribute('data-st', '0');
        }
    }
}

export function chStatB(e, inps) {
    let el = e.target;
    inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
    if (inps[el.id]) {
        el.setAttribute("data-mod", '0');
    } else {
        el.setAttribute("data-mod", '1');
    }
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +inps[el.id]);
}

export function ele (x, par, inps) {
    if(!inps[par]) inps[par] = x;
}

export function setTyp(x) {
    type = x;
    if(eventSource.readyState == EventSource.OPEN) setInfo();
    eventSource.addEventListener('connect', onCon, false);
    eventSource.addEventListener('chContactC', chContactC, false);
}

function onCon() {
    setInfo();
}

function setInfo() {
    send({
        type: type,
        role: cState.role,
        uuid: cState.uuid
    }, 'POST', "contacts", "getContacts")
        .then(data => {
            console.log(data);
            if(data.error == false){
                dispatch(changeContacts(CHANGE_CONTACT_GL, type, data.body));
            }
        });
}

function chContact (inp, p, p1) {
    console.log("chContact");
    send({
        uuid: cState.uuid,
        p: p,
        p1: p1,
        val: inp,
        role: cState.role
    }, 'POST', "contacts", "chContact");
}

function chContactC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeContacts(CHANGE_CONTACT_PARAM, type, msg.val, msg.p, msg.p1));
}

export function setActNew(name) {
    gr.group = name;
}

export function ContactMain() {
    cState = useSelector(states);
    dispatch = useDispatch();
    gr.groups = {
        0: {
            nam: "Контакты портала",
            linke: "por"
        },
        1: {
            nam: "Контакты учебного центра",
            linke: "yo"
        }
    };
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount ContactMain.jsx");
        setActived(2);
        return function() {
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('chContactC', chContactC);
            console.log("I was triggered during componentWillUnmount ContactMain.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate ContactMain.jsx');
    });
    return (
        <div className={contactCSS.AppHeader}>
            {(cState.auth && cState.role != 4) &&
                <div className={contactCSS.pane}>
                    <Pane gro={gr}/>
                </div>
            }
            <Outlet />
        </div>
    )
}
export default ContactMain;