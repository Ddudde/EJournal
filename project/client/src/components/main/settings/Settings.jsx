import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import settingsCSS from './settings.module.css';
import {checkbox, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {
    CHANGE_DIALOG,
    CHANGE_DIALOG_BUT,
    CHANGE_DIALOG_DEL,
    CHANGE_EVENTS_CLEAR,
    CHANGE_EVENTS_VISIBLE,
    CHANGE_STATE,
    changeCB,
    changeDialog,
    changeEvents,
    changeState
} from "../../../store/actions";
import ran from "../../../media/random.png";
import button from "../../button.module.css";
import CheckBox from "../../other/checkBox/CheckBox";
import ls1 from "../../../media/ls-icon1.png";
import ls2 from "../../../media/ls-icon2.png";
import ls3 from "../../../media/ls-icon3.png";
import {addEvent, eventSource, remEvent, sendToServer, setActived} from "../Main";

import {cAuth, cSettings} from "../../other/Controllers";

let dispatch, elem, cState, emailSt, els, checkBoxInfo, emailCode, emailCodePas;
emailSt = true;
elem = {npasinp : undefined, powpasinp : undefined, zambut : undefined, secBut : undefined, emBut : undefined, emInp : undefined, secinp : undefined, emal : undefined, codEm : undefined, emBlock : undefined, zamBlock : undefined, zamBlockFir : undefined};
els = {emInp: undefined, secinp: undefined, npasinp: undefined, powpasinp: undefined, warnUnsetSecFr: undefined, warnErrSecFr: undefined, warnErrEm: undefined};
emailCode = {
    obj: getEmail("Подтвердите E-Mail"),
    buts: {
        0 : {
            text: "ГОТОВО!",
            fun: () => checkCodeEmail(),
            enab: false
        },
        1 : {
            text: "ОТМЕНА",
            fun: () => dispatch(changeDialog(CHANGE_DIALOG_DEL)),
            enab: true
        }
    }
};
emailCodePas = {
    obj: getEmail("Изменение пароля"),
    buts: {
        0 : {
            text: "ГОТОВО!",
            fun: () => checkPasCodeEmail(),
            enab: false
        },
        1 : {
            text: "ОТМЕНА",
            fun: () => dispatch(changeDialog(CHANGE_DIALOG_DEL)),
            enab: true
        }
    }
};

function inpchr(event) {
    var el = event.target;
    if (el.validity.patternMismatch || el.value.length == 0) {
        el.setAttribute("data-mod", '1');
    } else {
        el.setAttribute("data-mod", '0');
    }
}

function onEditPass(e) {
    let par = e.target.parentElement;
    par.setAttribute('data-mod', '1');
}

function onChSt(e) {
    let par = e.target.parentElement.parentElement;
    emailSt = !emailSt;
    chStatB({target:emailSt ? elem.emInp:elem.secinp});
    if(!emailSt && !cState.secFr){
        if (els.warnUnsetSecFr == undefined) {
            els.warnUnsetSecFr = addEvent("Секретная фраза не установлена");
        }
    } else if(els.warnUnsetSecFr != undefined) {
        remEvent(els.warnUnsetSecFr);
        els.warnUnsetSecFr = undefined;
    }
    if(els.warnErrSecFr != undefined) {
        remEvent(els.warnErrSecFr);
        els.warnErrSecFr = undefined;
    }
    if(els.warnErrEm != undefined) {
        remEvent(els.warnErrEm);
        els.warnErrEm = undefined;
    }
    par.setAttribute('data-mod', +emailSt);
}

function onCloseBlock(e) {
    let par = e.target.classList.contains("clA") ? e.target.parentElement.parentElement : e.target.parentElement.parentElement.parentElement;
    par.setAttribute('data-mod', '0');
}

function getEmail(title) {
    return <div className={settingsCSS.code}>
        <div className={settingsCSS.zag}>
            {title}
        </div>
        <div className={settingsCSS.raz}>
            В течение нескольких минут вам
            придёт письмо с кодом, который
            необходимо ввести в форму ниже.
        </div>
        <div className={settingsCSS.raz}>
            Код подтверждения:
            <span style={{color: "#F00"}}> *</span>
        </div>
        <input className={settingsCSS.inp} type="text" placeholder="Код подтверждения" onChange={chGotovo} id="codeR"
               ref={el => elem.codEm = el} required pattern="^[a-zA-Z0-9_]+$"/>
    </div>
}

function chGotovo(e) {
    let el = e.target;
    els[el.id] = el.value;
    dispatch(changeDialog(CHANGE_DIALOG_BUT, els[el.id], 0));
}

function onChSF(e) {
    let par, inp;
    par = e.target.parentElement.parentElement;
    inp = par.querySelector("input");
    sendToServer({
        id: 'chSecFR',
        valString: inp.value
    }, 'PATCH', cSettings+"chSettings")
        .then(data => {
            if(data.status == 200){
                onCloseBlock(e);
                inp.value = "";
                dispatch(changeState(CHANGE_STATE, "secFr", true));
                if(els.warnUnsetSecFr != undefined) {
                    remEvent(els.warnUnsetSecFr);
                    els.warnUnsetSecFr = undefined;
                }
                chStatB({target:emailSt ? elem.emInp:elem.secinp});
            }
        });
}

function chStatB(e) {
    let el = e.target, bool;
    els[el.id] = (!el.validity.patternMismatch && el.value.length != 0) ? el.value : undefined;
    bool = ((emailSt ? els.emInp != undefined : (els.secinp != undefined & cState.secFr)) & els.npasinp != undefined & els.powpasinp != undefined & (els.npasinp == els.powpasinp));
    elem.zambut.setAttribute("data-enable", +bool);
    if(els.npasinp == els.powpasinp) {
        if(els.warnPow != undefined) {
            remEvent(els.warnPow);
            els.warnPow = undefined;
        }
    } else if (els.warnPow == undefined) {
        els.warnPow = addEvent("Повторите новый пароль верно");
    }
}

function chStatAv(e) {
    sendToServer({
        id: 'chIco',
        valInt: e.target.firstChild.value
    }, 'PATCH', cSettings+"chSettings")
        .then(data => {
            if(data.status == 200){
                e.target.firstChild.checked = true;
                dispatch(changeState(CHANGE_STATE, "ico", e.target.firstChild.value));
            }
        });
}

function onCloseChPar(e) {
    if(els.warnPow != undefined) {
        remEvent(els.warnPow);
        els.warnPow = undefined;
    }
    onCloseBlock(e);
}

function onFinChPar(e) {
    sendToServer({
        emailSt: emailSt,
        email: emailSt ? els.emInp : undefined,
        secFr: emailSt ? undefined : els.secinp,
        nPar : els.npasinp
    }, 'PATCH', cSettings+"chPass")
        .then(data => {
            if(data.status == 200){
                if(emailSt) {
                    dispatch(changeDialog(CHANGE_DIALOG, emailCodePas));
                } else {
                    onCloseBlock(e);
                }
                if(els.warnErrSecFr != undefined) {
                    remEvent(els.warnErrSecFr);
                    els.warnErrSecFr = undefined;
                }
                if(els.warnErrEm != undefined) {
                    remEvent(els.warnErrEm);
                    els.warnErrEm = undefined;
                }
            } else if(data.body.error == "email" && els.warnErrEm == undefined){
                els.warnErrEm = addEvent("Введённая почта неверна, попробуйте воспользоваться секретной фразой");
            } else if(data.body.error == "secFr" && els.warnErrSecFr == undefined){
                els.warnErrSecFr = addEvent("Секретная фраза неверна, попробуйте воспользоваться электронной почтой");
            }
        });
}

function chNotif(id) {
    console.log(id, checkBoxInfo[id]);
    if(id == "checkbox_hints") {
        dispatch(changeEvents(CHANGE_EVENTS_VISIBLE, !checkBoxInfo[id]));
    }
    sendToServer({
        id: id,
        val: !checkBoxInfo[id]
    }, 'PATCH', cSettings+"chSettings");
}

function chStatSb(e) {
    let el = e.target;
    elem.secBut.setAttribute("data-enable", +(el ? el.value.length != 0 : false));
}

function chStatEm(e) {
    let el = e.target;
    elem.emBut.setAttribute("data-enable", +(el ? el.value.length != 0 : false));
}

export function gen_pas(e){
    let password, symbols;
    password = "";
    symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < 15; i++){
        password += symbols.charAt(Math.floor(Math.random() * symbols.length));
    }
    for(let el of [elem.npasinp, elem.powpasinp]){
        el.value = password;
        inpchr({target:el});
        chStatB({target:el});
    }
    navigator.clipboard.writeText(password);
    addEvent(`Сгенерирован пароль: ${password}. Он скопирован в буфер обмена`, 10);
}

export function setSettings(dis) {
    if(dis) dispatch = dis;
    sendToServer(0, 'GET', cSettings+"getSettings")
        .then(data => {
            if(data.status == 200){
                for(let id in data.body) {
                    dispatch(changeCB(id, !data.body[id]));
                    if(id == "checkbox_hints") {
                        dispatch(changeEvents(CHANGE_EVENTS_VISIBLE, data.body[id]));
                    }
                }
                if(elem.zamBlock) elem.zamBlock.dataset.mod = +(cState.secFr || false);
            }
        });
}

function onCon(e) {
    sendToServer({
        type: "SETTINGS"
    }, 'PATCH',  cAuth+"infCon");
}

function checkCodeEmail() {
    sendToServer({
        emailCode: elem.codEm.value,
        email: elem.emal.value
    }, 'PATCH', cSettings+"checkCodeEmail")
        .then(data => {
            if(data.status == 200){
                dispatch(changeDialog(CHANGE_DIALOG_DEL));
                addEvent("Почта подтверждена успешно!", 10);
                elem.emBlock.dataset.mod = '0';
                dispatch(changeState(CHANGE_STATE, "email", true));
            } else {
                addEvent("Код подтверждения к почте, неверный", 10);
            }
        });
}

function checkPasCodeEmail() {
    sendToServer({
        emailCode: elem.codEm.value,
        nPar : els.npasinp
    }, 'PATCH', cSettings+"checkPasCodeEmail")
        .then(data => {
            if(data.status == 200){
                dispatch(changeDialog(CHANGE_DIALOG_DEL));
                addEvent("Код верный, пароль изменён успешно!", 10);
                elem.zamBlockFir.dataset.mod = '0';
            } else {
                addEvent("Код подтверждения, неверный", 10);
            }
        });
}

function startEmail() {
    sendToServer({
        email: elem.emal.value
    }, 'PATCH', cSettings+"startEmail")
        .then(data => {
            if(data.status == 200){
                dispatch(changeDialog(CHANGE_DIALOG, emailCode));
            }
        });
}

export function Settings() {
    checkBoxInfo = useSelector(checkbox);
    dispatch = useDispatch();
    cState = useSelector(states);
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        setActived(".panSet");
        if(eventSource.readyState == EventSource.OPEN) onCon();
        console.log("I was triggered during componentDidMount Settings.jsx");
        document.querySelector("#ch" + cState.ico).checked = true;
        eventSource.addEventListener('connect', onCon, false);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            eventSource.removeEventListener('connect', onCon);
            console.log("I was triggered during componentWillUnmount Settings.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Settings.jsx');
    });
    return <div className={settingsCSS.AppHeader}>
        <Helmet>
            <title>Настройки</title>
        </Helmet>
        <div className={settingsCSS.blockPro}>
            <div className={settingsCSS.pro}>
                <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_hints")}>
                    <CheckBox state={+true} text={"Включить подсказки"} checkbox_id={"checkbox_hints"}/>
                </div>
                <div className={settingsCSS.nav_iZag}>
                    <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_notify")}>
                        <CheckBox text={"Включить уведомления"} checkbox_id={"checkbox_notify"}/>
                    </div>
                    <div className={settingsCSS.nav_iZag} data-act={+(checkBoxInfo.checkbox_notify || false)}>
                        {(cState.role < 3) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_notify_sched")}>
                            <CheckBox text={"Уведомления о изменении в расписании"} checkbox_id={"checkbox_notify_sched"}/>
                        </div>}
                        {(cState.role < 2) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_notify_marks")}>
                            <CheckBox text={"Уведомления о новых оценках"} checkbox_id={"checkbox_notify_marks"}/>
                        </div>}
                        {(cState.role < 3) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_notify_yo")}>
                            <CheckBox text={"Присылать новые объявления учебного центра"} checkbox_id={"checkbox_notify_yo"}/>
                        </div>}
                        {(cState.role < 4) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_notify_por")}>
                            <CheckBox text={"Присылать новые объявления портала"} checkbox_id={"checkbox_notify_por"}/>
                        </div>}
                        {(cState.role == 4) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i} onClick={e=>chNotif("checkbox_notify_new_sch")}>
                            <CheckBox text={"Присылать уведомления о новых заявках школ"} checkbox_id={"checkbox_notify_new_sch"}/>
                        </div>}
                    </div>
                </div>
                <div className={settingsCSS.nav_iZag} data-mod="1" data-act={+(cState.secFr || cState.email)} ref={el=>elem.zamBlockFir = el}>
                    <div className={settingsCSS.nav_i+" "+settingsCSS.link} id={settingsCSS.nav_i} data-act='1' onClick={onEditPass}>
                        Сменить пароль
                    </div>
                    <div className={settingsCSS.block} data-mod='0' ref={el=>elem.zamBlock = el}>
                        <div className={settingsCSS.pasBlock+" "+settingsCSS.emailBlock}>
                            <input className={settingsCSS.inp} onChange={chStatB} onInput={inpchr} ref={el=>elem.emInp = el} id="emInp" placeholder="E-Mail" type="email"/>
                            <div className={button.button+" "+settingsCSS.marg} data-mod="2" onClick={onChSt}>
                                Заменить на секретную фразу
                            </div>
                        </div>
                        <div className={settingsCSS.pasBlock+" "+settingsCSS.frp}>
                            <input className={settingsCSS.inp} onChange={chStatB} onInput={inpchr} ref={el=>elem.secinp = el} id="secinp" placeholder="Секретная фраза" type="password" pattern="^[a-zA-Z0-9]+$"/>
                            <div className={button.button+" "+settingsCSS.marg} data-mod="2" onClick={onChSt}>
                                Заменить на e-mail
                            </div>
                        </div>
                        <div className={settingsCSS.pasBlock}>
                            <input className={settingsCSS.inp} ref={el=>elem.npasinp = el} onChange={chStatB} onInput={inpchr} id="npasinp" placeholder="Новый пароль" type="password" autoComplete="new-password" pattern="^[a-zA-Z0-9]+$"/>
                            <div className={button.button+" "+settingsCSS.marg} data-mod="2" onClick={gen_pas}>
                                <img src={ran} className={settingsCSS.randimg} alt=""/>
                                Случайный пароль
                            </div>
                        </div>
                        <input className={settingsCSS.inp+" "+settingsCSS.inpPass} ref={el=>elem.powpasinp = el} id="powpasinp" onChange={chStatB} onInput={inpchr} placeholder="Повторите пароль" type="password" autoComplete="new-password" pattern="^[a-zA-Z0-9]+$"/>
                        <div className={settingsCSS.blockKnops}>
                            <div className={button.button} ref={el=>elem.zambut = el} data-mod="2" data-enable="0" onClick={onFinChPar}>
                                Замена!
                            </div>
                            <div className={button.button} data-mod="2" onClick={onCloseChPar}>
                                Отменить
                            </div>
                        </div>
                    </div>
                </div>
                <div className={settingsCSS.nav_iZag} data-mod="0">
                    <div className={settingsCSS.nav_i+" "+settingsCSS.link} id={settingsCSS.nav_i} onClick={onEditPass}>
                        Сменить аватар
                    </div>
                    <div className={settingsCSS.block}>
                        <div className={settingsCSS.logo}>
                            <p style={{marginBlock: "0.5vw"}}>Выберите аватар для профиля:</p>
                            <div className={settingsCSS.blockAva} onClick={chStatAv}>
                                <input id="ch1" name="ico" type="radio" value="1"/>
                                <img className={settingsCSS.logoi} src={ls1} alt=""/>
                            </div>
                            <div className={settingsCSS.blockAva} onClick={chStatAv}>
                                <input id="ch2" name="ico" type="radio" value="2"/>
                                <img className={settingsCSS.logoi} src={ls2} alt=""/>
                            </div>
                            <div className={settingsCSS.blockAva} onClick={chStatAv}>
                                <input id="ch3" name="ico" type="radio" value="3"/>
                                <img className={settingsCSS.logoi} src={ls3} alt=""/>
                            </div>
                        </div>
                        <div className={button.button+' clA '+settingsCSS.marg} data-mod="2" style={{width:"fit-content"}} onClick={onCloseBlock}>
                            Закрыть меню выбора
                        </div>
                    </div>
                </div>
                <div className={settingsCSS.nav_iZag} data-mod="0" ref={el => elem.emBlock = el}>
                    <div className={settingsCSS.nav_i+" "+settingsCSS.link} id={settingsCSS.nav_i} onClick={onEditPass}>
                        {cState.email? "Изменить" : "Добавить"} электронную почту
                    </div>
                    <div className={settingsCSS.block}>
                        <input className={settingsCSS.inp+" "+settingsCSS.inpPass} ref={el => elem.emal = el} onChange={chStatEm} onInput={inpchr} placeholder="Электронная почта" type="email"/>
                        <div className={settingsCSS.blockKnops}>
                            <div className={button.button} data-mod="2" ref={el=>elem.emBut = el} data-enable="0" onClick={startEmail}>
                                Подтвердить
                            </div>
                            <div className={button.button} data-mod="2" onClick={onCloseBlock}>
                                Отменить
                            </div>
                        </div>
                    </div>
                </div>
                <div className={settingsCSS.nav_iZag} data-mod="0">
                    <div className={settingsCSS.nav_i+" "+settingsCSS.link} id={settingsCSS.nav_i} onClick={onEditPass}>
                        {cState.secFr? "Изменить" : "Добавить"} секретную фразу
                    </div>
                    <div className={settingsCSS.block}>
                        <input className={settingsCSS.inp+" "+settingsCSS.inpPass} onChange={chStatSb} onInput={inpchr} placeholder="Секретная фраза" type="password" pattern="^[a-zA-Z0-9]+$"/>
                        <div className={settingsCSS.blockKnops}>
                            <div className={button.button} data-mod="2" ref={el=>elem.secBut = el} data-enable="0" onClick={onChSF}>
                                Подтвердить
                            </div>
                            <div className={button.button} data-mod="2" onClick={onCloseBlock}>
                                Отменить
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
}
export default Settings;