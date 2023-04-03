import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import settingsCSS from './settings.module.css';
import {checkbox, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {CHANGE_EVENTS_CLEAR, CHANGE_STATE, changeEvents, changeState} from "../../../store/actions";
import ran from "../../../media/random.png";
import button from "../../button.module.css";
import CheckBox from "../../other/checkBox/CheckBox";
import ls1 from "../../../media/ls-icon1.png";
import ls2 from "../../../media/ls-icon2.png";
import ls3 from "../../../media/ls-icon3.png";
import {addEvent, eventSource, remEvent, send, setActived} from "../Main";

let dispatch, elem, cState, oldPasSt, els;
oldPasSt = true;
elem = {npasinp : undefined, powpasinp : undefined, zambut : undefined, zambut1 : undefined, oldinp : undefined, secinp : undefined};
els = {oldinp: undefined, secinp: undefined, npasinp: undefined, powpasinp: undefined};

function inpchr(event) {
    var el = event.target;
    if (el.validity.patternMismatch || el.value.length == 0) {
        el.setAttribute("data-mod", '1');
        // warner.style.display = "inline";
    } else {
        el.setAttribute("data-mod", '0');
        // warner.style.display = "none";
    }
}

function onEditPass(e) {
    let par = e.target.parentElement;
    par.setAttribute('data-mod', '1');
}

function onChSt(e) {
    let par = e.target.parentElement.parentElement;
    oldPasSt = !oldPasSt;
    chStatB({target:oldPasSt ? elem.oldinp:elem.secinp});
    if(!oldPasSt && !cState.secFr){
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
    if(els.warnErrPar != undefined) {
        remEvent(els.warnErrPar);
        els.warnErrPar = undefined;
    }
    par.setAttribute('data-mod', oldPasSt ? '0' : '1');
}

function onClosePas(e) {
    let par = e.target.classList.contains("clA") ? e.target.parentElement.parentElement : e.target.parentElement.parentElement.parentElement;
    par.setAttribute('data-mod', '0');
}

function onChSF(e) {
    let par, inp;
    par = e.target.parentElement.parentElement;
    inp = par.querySelector("input");
    send({
        login: cState.login,
        secFR: inp.value
    }, 'POST', "settings", "chSecFR")
        .then(data => {
            if(data.error == false){
                onClosePas(e);
                inp.value = "";
                dispatch(changeState(CHANGE_STATE, "secFr", true));
                if(els.warnUnsetSecFr != undefined) {
                    remEvent(els.warnUnsetSecFr);
                    els.warnUnsetSecFr = undefined;
                }
                chStatB({target:oldPasSt ? elem.oldinp:elem.secinp});
            }
        });
}

function chStatB(e) {
    let el = e.target, bool;
    els[el.id] = (!el.validity.patternMismatch && el.value.length != 0) ? el.value : undefined;
    bool = ((oldPasSt ? els.oldinp != undefined : (els.secinp != undefined & cState.secFr)) & els.npasinp != undefined & els.powpasinp != undefined & (els.npasinp == els.powpasinp));
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
    send({
        login: cState.login,
        ico: e.target.firstChild.value
    }, 'POST', "settings", "chIco")
        .then(data => {
            if(data.error == false){
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
    onClosePas(e);
}

function onFinChPar(e) {
    send({
        login: cState.login,
        oPar: oldPasSt ? els.oldinp : undefined,
        secFr: oldPasSt ? undefined : els.secinp,
        nPar : els.npasinp
    }, 'POST', "settings", "chPass")
        .then(data => {
            if(data.error == false){
                onClosePas(e);
                if(els.warnErrSecFr != undefined) {
                    remEvent(els.warnErrSecFr);
                    els.warnErrSecFr = undefined;
                }
                if(els.warnErrPar != undefined) {
                    remEvent(els.warnErrPar);
                    els.warnErrPar = undefined;
                }
            } else if(data.error == 2 && els.warnErrPar == undefined){
                els.warnErrPar = addEvent("Старый пароль неверен, попробуйте воспользоваться секретной фразой");
            } else if(data.error == 3 && els.warnErrSecFr == undefined){
                els.warnErrSecFr = addEvent("Секретная фраза неверна, попробуйте воспользоваться старым паролем");
            }
        });
}

function chStatSb1(e) {
    let el = e.target;
    elem.zambut1.setAttribute("data-enable", +(el ? el.value.length != 0 : false));
}

function onCon(e) {
    send({
        type: "SETTINGS",
        uuid: cState.uuid
    }, 'POST', "auth", "infCon");
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

export function Settings() {
    const checkBoxInfo = useSelector(checkbox);
    dispatch = useDispatch();
    cState = useSelector(states);
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        setActived(".panSet");
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
    return (
        <div className={settingsCSS.AppHeader}>
            <Helmet>
                <title>Настройки</title>
            </Helmet>
            <div className={settingsCSS.blockPro}>
                <div className={settingsCSS.pro}>
                    <div className={settingsCSS.nav_i} id={settingsCSS.nav_i}>
                        <CheckBox text={"Включить уведомления"} checkbox_id={"checkbox_notify"}/>
                    </div>
                    <div className={settingsCSS.nav_iZag+" "+settingsCSS.blockNotif} data-act={(checkBoxInfo.checkbox_notify || false) ? '1' : '0'}>
                        {(cState.role < 3) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i}>
                            <CheckBox state={+true} text={"Уведомления о изменении в расписании"} checkbox_id={"checkbox_notify_sched"}/>
                        </div>}
                        {(cState.role < 2) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i}>
                            <CheckBox state={+true} text={"Уведомления о новых оценках"} checkbox_id={"checkbox_notify_marks"}/>
                        </div>}
                        {(cState.role < 3) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i}>
                            <CheckBox state={+true} text={"Присылать новые объявления учебного центра"} checkbox_id={"checkbox_notify_yo"}/>
                        </div>}
                        {(cState.role < 4) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i}>
                            <CheckBox state={+true} text={"Присылать новые объявления портала"} checkbox_id={"checkbox_notify_por"}/>
                        </div>}
                        {(cState.role == 4) && <div className={settingsCSS.nav_i} id={settingsCSS.nav_i}>
                            <CheckBox state={+true} text={"Присылать уведомления о новых заявках школ"} checkbox_id={"checkbox_notify_new_sch"}/>
                        </div>}
                    </div>
                    <div className={settingsCSS.nav_iZag} data-mod="0">
                        <div className={settingsCSS.nav_i+" "+settingsCSS.link} id={settingsCSS.nav_i} data-act='1' onClick={onEditPass}>
                            Сменить пароль
                        </div>
                        <div className={settingsCSS.block} data-mod='0'>
                            <div className={settingsCSS.pasBlock+" "+settingsCSS.oldp}>
                                <input className={settingsCSS.inp} onChange={chStatB} onInput={inpchr} ref={el=>elem.oldinp = el} id="oldinp" placeholder="Старый пароль" type="password" pattern="^[a-zA-Z0-9]+$"/>
                                <div className={button.button+" "+settingsCSS.marg} data-mod="2" onClick={onChSt}>
                                    Забыл пароль?
                                </div>
                            </div>
                            <div className={settingsCSS.pasBlock+" "+settingsCSS.frp}>
                                <input className={settingsCSS.inp} onChange={chStatB} onInput={inpchr} ref={el=>elem.secinp = el} id="secinp" placeholder="Секретная фраза" type="password" pattern="^[a-zA-Z0-9]+$"/>
                                <div className={button.button+" "+settingsCSS.marg} data-mod="2" onClick={onChSt}>
                                    Вспомнил пароль
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
                            <div className={button.button+' clA '+settingsCSS.marg} data-mod="2" style={{width:"fit-content"}} onClick={onClosePas}>
                                Закрыть меню выбора
                            </div>
                        </div>
                    </div>
                    <div className={settingsCSS.nav_iZag} data-mod="0">
                        <div className={settingsCSS.nav_i+" "+settingsCSS.link} id={settingsCSS.nav_i} onClick={onEditPass}>
                            {cState.secFr? "Изменить" : "Добавить"} секретную фразу
                        </div>
                        <div className={settingsCSS.block}>
                            <input className={settingsCSS.inp+" "+settingsCSS.inpPass} onChange={chStatSb1} onInput={inpchr} placeholder="Секретная фраза" type="password" pattern="^[a-zA-Z0-9]+$"/>
                            <div className={settingsCSS.blockKnops}>
                                <div className={button.button} data-mod="2" ref={el=>elem.zambut1 = el} data-enable="0" onClick={onChSF}>
                                    Подтвердить
                                </div>
                                <div className={button.button} data-mod="2" onClick={onClosePas}>
                                    Отменить
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
export default Settings;