import React, {useEffect, useReducer, useRef} from "react";
import warn from '../../media/warning.png';
import ls1 from '../../media/ls-icon1.png';
import ls2 from '../../media/ls-icon2.png';
import ls3 from '../../media/ls-icon3.png';
import ran from '../../media/random.png';
import pedagog from '../../media/start/pedagog.jpg';
import roditelyam from '../../media/start/roditelyam.jpg';
import zavuch from '../../media/start/zavuch.jpg';
import detyam from '../../media/start/detyam.jpeg';
import left from '../../media/start/left.png';
import sta from '../../media/start/start.gif';
import start from './start.module.css';
import button from "../button.module.css";
import {Helmet} from "react-helmet-async";
import CheckBox from "../other/checkBox/CheckBox";
import {useDispatch, useSelector} from "react-redux";
import {checkbox, indicators, states} from "../../store/selector";
import {
    CHANGE_DIALOG,
    CHANGE_DIALOG_BUT,
    CHANGE_DIALOG_DEL,
    CHANGE_EVENTS_CLEAR,
    CHANGE_STATE,
    CHANGE_STATE_GL,
    changeDialog,
    changeEvents,
    changeInd,
    changeIndNext,
    changeIndPrev,
    changeState
} from "../../store/actions";
import {addEvent, eventSource, remEvent, sendToServer, setActived} from "../main/Main";
import {Link, useNavigate, useParams} from "react-router-dom"
import ErrFound from "../other/error/ErrFound";
import {setSettings} from "../main/settings/Settings";
import {cAuth, cSettings} from "../other/Controllers";

let dispatch, warns, timer, indicInfo, cState, navigate, checkBoxInfo, elem, els, textYesInvNR, textNoInv, blocks, licField, selEmailR, emailCode, code, prop, selEmailZ, emailCodePas;
elem = {regbut: undefined, vxbut: undefined, g_id: undefined, logv: undefined, pasv: undefined, logz: undefined, blockRecR: undefined, emalR:undefined, codEm:undefined, logoR:undefined, blockRecZ:undefined, emalZ:undefined, vxodBlock:undefined};
textNoInv = "Приглашение неверно или недействительно.";
textYesInvNR = "К действующему аккаунту была добавлена новая роль.";
els = {logz: 0, pasnz: 0, paspz: 0, logv: 0, pasv: 0, logr: 0, pasr: 0, ppasr: 0, emalR: 0, secFrR: 0, secFrZ: 0, emalZ: 0};
warns = {pat: undefined, empt: undefined, pow: undefined};
selEmailR = true;
selEmailZ = true;
blocks = [
    {
        name: "Завучам",
        text: "Немного информации об портале для завучей",
        img: zavuch,
        link: "tutor/sch"
    },
    {
        name: "Педагогам",
        text: "Немного информации об портале для педагогов",
        img: pedagog,
        link: "tutor/tea"
    },
    {
        name: "Родителям",
        text: "Немного информации об портале для родителей",
        img: roditelyam,
        link: "tutor/par"
    },
    {
        name: "Детям",
        text: "Немного информации об портале для детей",
        img: detyam,
        link: "tutor/kid"
    }
];
licField = {
    obj: <div className={start.lic_text}>
        {getLic()}
    </div>,
    buts: {
        0 : {
            text: "Прочитал",
            fun: () => dispatch(changeDialog(CHANGE_DIALOG_DEL)),
            enab: true
        }
    }
};
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
    obj: getEmail("Восстановление пароля"),
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
let [_, forceUpdate] = [];

function gen_pas(e){
    let par, password, symbols;
    par = e.target.parentElement.parentElement;
    password = "";
    symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < 15; i++){
        password += symbols.charAt(Math.floor(Math.random() * symbols.length));
    }
    for(let el of par.querySelectorAll("." + start.inps+"[type='password']")){
        el.value = password;
        inpchr({target:el});
        par.classList.contains(start.reg) ? chStatRb({target:el}) : chStatZb({target:el});
    }
    navigator.clipboard.writeText(password);
    addEvent(`Сгенерирован пароль: ${password}. Он скопирован в буфер обмена`, 10);
}

function checkPasCodeEmail() {
    sendToServer({
        login: els.logz,
        emailCode: elem.codEm.value,
        nPar : els.pasnz
    }, 'PATCH', cSettings+"checkPasCodeEmail")
        .then(data => {
            if(data.status == 200){
                dispatch(changeDialog(CHANGE_DIALOG_DEL));
                addEvent("Код верный, пароль изменён успешно!", 10);
                elem.vxodBlock.dataset.mod = '0';
            } else {
                addEvent("Код подтверждения, неверный", 10);
            }
        });
}

function checkCodeEmail() {
    sendToServer({
        invCod: code,
        emailCode: elem.codEm.value,
        email: elem.emalR.value
    }, 'PATCH', cSettings+"checkCodeEmail")
        .then(data => {
            if(data.status == 200){
                dispatch(changeDialog(CHANGE_DIALOG_DEL));
                addEvent("Почта подтверждена успешно!", 10);
                rego();
            } else {
                addEvent("Код подтверждения к почте, неверный", 10);
            }
        });
}

function startEmail() {
    sendToServer({
        invCod: code,
        email: elem.emalR.value
    }, 'PATCH', cSettings+"startEmail")
        .then(data => {
            if(data.status == 200){
                dispatch(changeDialog(CHANGE_DIALOG, emailCode));
            }
        });
}

function preRego(){
    if(selEmailR) {
        startEmail();
    } else {
        rego();
    }
}

function rego(){
    let ch = elem.logoR.querySelector("input[checked]");
    console.log(prop);
    if(els.pasr && els.logr){
        sendToServer({
            login: els.logr,
            par: els.pasr,
            ico: ch.value,
            mod: prop.mod,
            secFr: selEmailR ? undefined : els.secFrR,
            code: code
        }, 'POST', cAuth+"reg")
            .then(data => {
                if(data.status == 201){
                    onvxod({target: elem.logoR.firstElementChild});
                    navigate("/");
                    if(warns.logR != undefined) {
                        remEvent(warns.logR);
                        warns.logR = undefined;
                    }
                } else if(data.body.error == "noInv"){
                    addEvent(textNoInv, 10);
                } else if(warns.logR == undefined && prop.mod == undefined){
                    warns.logR = addEvent("Логин занят, попробуйте изменить");
                }
            });
    }
}

function vxo(){
    sendToServer({
        login: elem.logv.value,
        password: elem.pasv.value,
        notifToken: localStorage.getItem("notifToken"),
        permis: Notification.permission == "granted"
    }, 'POST', cAuth+"auth")
        .then(data => {
            if(data.status == 200){
            // if(data.error == false && data.body.auth){
                console.log(data);
                dispatch(changeState(CHANGE_STATE_GL, undefined, data.body));
                setSettings(dispatch);
            } else {
                addEvent("Неверный логин или пароль", 10);
            }
        });
}

function inpchr(e){
    var el = e.target;
    if(!e.inputType) return;
    if (el.validity.patternMismatch || el.value.length == 0) {
        el.setAttribute("data-mod", '1');
        if(el.value.length == 0){
            if(warns.empt == undefined) {
                warns.empt = addEvent("Необходимо заполнить поле");
                if(warns.pat != undefined) {
                    remEvent(warns.pat);
                    warns.pat = undefined;
                }
            }
        } else if(warns.pat == undefined) {
            warns.pat = addEvent("Допустимы только латиница, цифры или дефис/нижнее подчёркивание");
            if(warns.empt != undefined) {
                remEvent(warns.empt);
                warns.empt = undefined;
            }
        }
    } else {
        el.setAttribute("data-mod", '0');
        if(warns.pat != undefined) {
            remEvent(warns.pat);
            warns.pat = undefined;
        } else if(warns.empt != undefined) {
            remEvent(warns.empt);
            warns.empt = undefined;
        }
    }
}

function checkCaps(event) {
    var caps = event.getModifierState && event.getModifierState('CapsLock');
    for(let el of document.getElementsByClassName(start.warn)){
        el.style.opacity = +caps;
    }
}

function onvxod(e){
    let par = e.target.parentElement.parentElement;
    par.setAttribute('data-mod', 0);
    par = par.parentElement;
    par.setAttribute('data-mod', 0);
    dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
    warns = {};
}

function onreg(e){
    let par = e.target.parentElement.parentElement;
    par.setAttribute('data-mod', 1);
    par = par.parentElement;
    par.setAttribute('data-mod', 1);
    dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
    warns = {};
}

function reset_timer() {
    clearInterval(timer);
    timer = setInterval(function() { dispatch(changeIndNext(indicInfo.actived)); }, 5000);
}

function changeSelEmailR() {
    selEmailR = !selEmailR;
    elem.blockRecR.dataset.selemail = +selEmailR;
    chStatRb();
}

function changeSelEmailZ() {
    selEmailZ = !selEmailZ;
    elem.blockRecZ.dataset.selemail = +selEmailZ;
    chStatZb();
}

function chStatRb(e) {
    if(e) {
        let el = e.target;
        els[el.id] = (el.validity.patternMismatch || el.validity.typeMismatch) ? false : el.value;
    }
    els.regb = (checkBoxInfo.checkbox_lic && els.logr && els.pasr && els.ppasr && (els.pasr == els.ppasr) && (selEmailR ? els.emalR : els.secFrR)) || false;
    elem.regbut.setAttribute("data-enable", +els.regb);
    if(els.pasr == els.ppasr) {
        if(warns.pow != undefined) {
            remEvent(warns.pow);
            warns.pow = undefined;
        }
    } else if (warns.pow == undefined) {
        warns.pow = addEvent("Повторите новый пароль верно");
    }
}

function chStatVb(e, x) {
    let el = e.target;
    els[el.id] = x ? true : (el ? !el.validity.patternMismatch && el.value.length != 0 : false);
    elem.vxbut.setAttribute("data-enable", +((els.logv & els.pasv) || false));
}

function chStatAv(e) {
    e.target.firstChild.checked = true;
}

function onRec(e) {
    sendToServer({
        login: els.logz,
        emailSt: selEmailZ,
        email: selEmailZ ? els.emalZ : undefined,
        secFr: selEmailZ ? undefined : els.secFrZ,
        nPar : els.pasnz
    }, 'PATCH', cSettings+"chPass")
        .then(data => {
            if(data.status == 200){
                if(selEmailZ) {
                    dispatch(changeDialog(CHANGE_DIALOG, emailCodePas));
                } else {
                    e.target = e.target.parentElement;
                    onSmvz(e);
                }
                if(warns.chPass != undefined) {
                    remEvent(warns.chPass);
                    warns.chPass = undefined;
                }
            } else if(warns.chPass == undefined){
                let text = "Неверен логин или " + (selEmailZ ? "электронная почта" : "секретная фраза");
                warns.chPass = addEvent(text);
            }
        });
}

function onSmvz(e) {
    let par, mod;
    par = e.target.parentElement.parentElement.parentElement;
    mod = par.getAttribute('data-mod') == '0';
    par.setAttribute("data-mod", +mod);
    if(mod && warns.pow != undefined) {
        remEvent(warns.pow);
        warns.pow = undefined;
    }
}

function chStatZb(e) {
    if(e) {
        let el = e.target;
        els[el.id] = (el.validity.patternMismatch || el.validity.typeMismatch) ? false : el.value;
    }
    document.querySelector("#butL").setAttribute("data-enable", +((els.logz && (selEmailZ ? els.emalZ : els.secFrZ) && els.pasnz && els.paspz && (els.pasnz == els.paspz)) || false));
    if(els.pasnz == els.paspz) {
        if(warns.pow != undefined) {
            remEvent(warns.pow);
            warns.pow = undefined;
        }
    } else if (warns.pow == undefined) {
        warns.pow = addEvent("Повторите новый пароль верно");
    }
}

function unsetText(e) {
    for(let el of e.target.getElementsByClassName(start.g_block_text)){
        el.innerHTML = el.getAttribute("data-text");
    }
}

function onsetText(e) {
    for(let el of e.target.getElementsByClassName(start.g_block_text)){
        el.innerHTML = el.getAttribute("data-textm");
    }
}

function getEmail(title) {
    return <div className={start.code}>
        <div className={start.zag}>
            {title}
        </div>
        <div className={start.raz}>
            В течение нескольких минут вам
            придёт письмо с кодом, который
            необходимо ввести в форму ниже.
        </div>
        <div className={start.raz}>
            Код подтверждения:
            <span style={{color: "#F00"}}> *</span>
        </div>
        <input className={start.inps} type="text" placeholder="Код подтверждения" onChange={chGotovo} id="codeR"
               ref={el => elem.codEm = el} required pattern="^[a-zA-Z0-9_]+$"/>
    </div>
}

function chGotovo(e) {
    let el = e.target;
    els[el.id] = el.value;
    dispatch(changeDialog(CHANGE_DIALOG_BUT, els[el.id], 0));
}

function onCon(e) {
    sendToServer({
        type: "AUTH"
    }, 'PATCH', cAuth+"infCon");
}

export function Start(props) {
    checkBoxInfo = useSelector(checkbox);
    code = useParams().code;
    prop = props;
    navigate = useNavigate();
    cState = useSelector(states);
    indicInfo = useSelector(indicators);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    const isFirstUpdate = useRef(true);
    els.regb = (checkBoxInfo.checkbox_lic && els.logr && els.pasr && els.ppasr && (els.pasr == els.ppasr)) || false;
    dispatch = useDispatch();
    useEffect(() => {
        console.log("I was triggered during componentDidMount Start.jsx")
        chStatVb({target: elem.logv});
        chStatZb({target: elem.logz});
        if(code){
            sendToServer({
                code: code
            }, 'POST', cAuth+"checkInvCode")
                .then(data => {
                    if(data.status == 200){
                        if(props.mod == "inv") {
                            if (cState.auth) {
                                dispatch(changeState(CHANGE_STATE, "reaYes", true));
                            } else {
                                addEvent("Поздравляем, приглашение активно! Вам разрешено зарегистроваться.", 10);
                            }
                        } else {
                            addEvent("Аккаунт существует. Открыта возможность перерегистрации.", 10);
                        }
                    } else {
                        dispatch(changeState(CHANGE_STATE, "invErr", true));
                    }
                });
        }
        console.log(code);
        elem.g_id.addEventListener('mouseenter', onsetText);
        elem.g_id.addEventListener('mouseleave', unsetText);
        dispatch(changeInd(0, reset_timer));
        setActived(0);
        window.addEventListener('click', checkCaps);
        window.addEventListener('keydown', checkCaps);
        for(let el of document.querySelectorAll("input[placeholder]")){
            el.addEventListener('input', inpchr);
        }
        eventSource.addEventListener('connect', onCon, false);
        // dispatch(changeDialog(CHANGE_DIALOG, emailCode));
        // addEvent("Почта подтверждена успешно!", 10);
        // addEvent("Код подтверждения к почте, неверный", 10);
        return function() {
            clearInterval(timer);
            console.log("I was triggered during componentWillUnmount Start.jsx");
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            warns = {};
            eventSource.removeEventListener('connect', onCon);
            dispatch = undefined;
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        reset_timer();
        console.log('componentDidUpdate Start.jsx');
    });
    return <div className={start.AppHeader}>
        <Helmet>
            <title>Главная</title>
        </Helmet>
        <div className={start.block}>
            {cState.invErr &&
                <ErrFound text={textNoInv}/>
            }
            {cState.reaYes &&
                <ErrFound text={textYesInvNR}/>
            }
            {(cState.invErr || cState.reaYes) ? undefined : <>
                <div className={start.g}>
                    <div className={start.gH} ref={el=>elem.g_id=el}>
                        {blocks.map((param, i) =>
                            <Link className={start.g_block} to={param.link} key={i} data-act={indicInfo.actived == i ? "1" : "0"}>
                                <img src={param.img} className={start.pic_g} alt=""/>
                                <div className={start.g_block_text} data-text={param.name} data-textm={param.text}>
                                    {param.name}
                                </div>
                            </Link>
                        )}
                        <div className={start.g_block_shad}/>
                        <img src={left} className={start.pic_l} alt="" onClick={() => {dispatch(changeIndPrev(indicInfo.actived, reset_timer))}}/>
                        <img src={left} className={start.pic_r} alt="" onClick={() => {dispatch(changeIndNext(indicInfo.actived, reset_timer))}}/>
                        <div className={start.indic}>
                            <div className={start.indic_bl} id="ind_0" data-act={!indicInfo.actived ? "1" : "0"} onClick={() => {dispatch(changeInd(0, reset_timer))}}/>
                            <div className={start.indic_bl} id="ind_1" data-act={indicInfo.actived == 1 ? "1" : "0"} onClick={() => {dispatch(changeInd(1, reset_timer))}}/>
                            <div className={start.indic_bl} id="ind_2" data-act={indicInfo.actived == 2 ? "1" : "0"} onClick={() => {dispatch(changeInd(2, reset_timer))}}/>
                            <div className={start.indic_bl} id="ind_3" data-act={indicInfo.actived == 3 ? "1" : "0"} onClick={() => {dispatch(changeInd(3, reset_timer))}}/>
                        </div>
                    </div>
                </div>
                <div className={start.startimg}>
                    <div className={start.startimgText}>
                        Для авторизации проскролльте или нажмите на стрелки
                    </div>
                    <img src={sta} alt="" onClick={() => {window.scrollTo(0, window.innerHeight)}}/>
                </div>
            </>}
        </div>
        {(cState.invErr || cState.reaYes) ? undefined :
            <div className={start.block}>
                <div className={start.posit} data-mod="0">
                    <div className={start.help} data-enable={code ? '1' : '0'} data-mod="0">
                        <div className={start.r}>
                            Нет аккаунта? <span className={start.helpa} onClick={onreg}>Регистрация!</span>
                        </div>
                        <div className={start.v}>
                            Есть аккаунт? <span className={start.helpa} onClick={onvxod}>Вход!</span>
                        </div>
                    </div>
                    <form className={start.vxod} data-mod="0" ref={el=>elem.vxodBlock = el}>
                        <div className={start.vxo}>
                            <input className={start.inps} type="login" onChange={chStatVb}
                                   ref={el => elem.logv = el} placeholder="Логин" id="logv" autoComplete="username"
                                   required pattern="^[a-zA-Z0-9\-]+$"/>
                            <div className={start.dinp}>
                                <input className={start.inps} type="password" onChange={chStatVb}
                                       ref={el => elem.pasv = el} placeholder="Пароль" id="pasv"
                                       autoComplete="current-password" required pattern="^[a-zA-Z0-9_]+$"/>
                                <div className={start.nav_i + " " + start.zabpar} id={start.nav_i} onClick={onSmvz}>
                                    Забыли пароль?
                                </div>
                            </div>
                            <div className={start.dinp}>
                                <div className={start.dinpo}>
                                    <div className={start.warn + ' ' + start.warnc} id="warnc">
                                        <img src={warn} className={start.warnimg} alt=""/>
                                        Включён Caps Lock!
                                    </div>
                                </div>
                                <div className={button.button + ' ' + start.marg} ref={el => elem.vxbut = el}
                                     onClick={vxo}>
                                    ВОЙТИ!
                                </div>
                            </div>
                        </div>
                        <div className={start.zab}>
                            <input className={start.inps} ref={el => elem.logz = el} type="text"
                                   onChange={chStatZb} placeholder="Логин" id="logz" autoComplete="username"
                                   required pattern="^[a-zA-Z0-9\-]+$"/>
                            <div className={start.blockRec} data-selemail={+selEmailZ} ref={el=>elem.blockRecZ=el}>
                                <div className={start.email}>
                                    <div className={start.dinp}>
                                        <input className={start.inps} ref={el => elem.emalZ = el} type="email" placeholder="E-Mail" onChange={chStatZb} id="emalZ" required/>
                                        <span className={button.button + ' ' + start.marg} data-mod='2' onClick={changeSelEmailZ}>
                                            Заменить на секретную фразу
                                        </span>
                                    </div>
                                </div>
                                <div className={start.secFR}>
                                    <div className={start.dinp}>
                                        <input className={start.inps} type="text" placeholder="Секретная фраза" onChange={chStatZb} id="secFrZ"
                                               required pattern="^[a-zA-Z0-9_]+$"/>
                                        <span className={button.button + ' ' + start.marg} data-mod='2' onClick={changeSelEmailZ}>
                                            Заменить на e-mail
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div className={start.dinp}>
                                <input className={start.inps} type="password" onChange={chStatZb}
                                       placeholder="Новый пароль" id="pasnz" autoComplete="new-password" required
                                       pattern="^[a-zA-Z0-9_]+$"/>
                                <div className={button.button + ' ' + start.marg} data-mod='2' onClick={gen_pas}>
                                    <img src={ran} className={start.randimg} alt=""/>
                                    Случайный пароль
                                </div>
                            </div>
                            <div className={start.dinp}>
                                <input className={start.inps} type="password" onChange={chStatZb}
                                       placeholder="Подтвердите пароль" id="paspz" autoComplete="new-password"
                                       required pattern="^[a-zA-Z0-9_]+$"/>
                                <span className={start.warn + ' ' + start.marg} id="warncz">
                                    <img src={warn} className={start.warnimg} alt=""/>
                                    Включён Caps Lock!
                                </span>
                            </div>
                            <div className={start.dinp}>
                                <div className={start.dinpo}>
                                    <div className={button.button + ' ' + start.butZab} id="butL" data-mod="1"
                                         onClick={onRec}>
                                        Подтвердить
                                    </div>
                                </div>
                                <div className={button.button + ' ' + start.butZab + ' ' + start.marg} id="butR"
                                     data-mod="1" onClick={onSmvz}>
                                    Вспомнил пароль
                                </div>
                            </div>
                        </div>
                    </form>
                    <form className={start.reg} id="reg">
                        <div className={start.logo} ref={el => elem.logoR = el}>
                            <p style={{marginBlock: "0.5vw"}}>
                                Выберите аватар для профиля:
                                <span style={{color: "#F00"}}> *</span>
                            </p>
                            <div className={start.blockAva} onClick={chStatAv}>
                                <input id="ch1" name="ico" type="radio" value="1" defaultChecked/>
                                <img className={start.logoi} src={ls1} alt=""/>
                            </div>
                            <div className={start.blockAva} onClick={chStatAv}>
                                <input id="ch2" name="ico" type="radio" value="2"/>
                                <img className={start.logoi} src={ls2} alt=""/>
                            </div>
                            <div className={start.blockAva} onClick={chStatAv}>
                                <input id="ch3" name="ico" type="radio" value="3"/>
                                <img className={start.logoi} src={ls3} alt=""/>
                            </div>
                        </div>
                        <div className={start.raz}>
                            Логин:
                            <span style={{color: "#F00"}}> *</span>
                        </div>
                        <input className={start.inps} type="text" placeholder="Логин" onChange={chStatRb} id="logr"
                               autoComplete="username" required pattern="^[a-zA-Z0-9\-]+$"/>
                        <div className={start.raz}>
                            Пароль:
                            <span style={{color: "#F00"}}> *</span>
                        </div>
                        <input className={start.inps} type="password" placeholder="Пароль" onChange={chStatRb}
                               id="pasr" autoComplete="new-password" required pattern="^[a-zA-Z0-9_]+$"/>
                        <div className={start.raz}>
                            Повторите пароль:
                            <span style={{color: "#F00"}}> *</span>
                        </div>
                        <div className={start.dinp}>
                            <input className={start.inps} type="password" placeholder="Повторите пароль"
                                   onChange={chStatRb} id="ppasr" autoComplete="new-password" required
                                   pattern="^[a-zA-Z0-9_]+$"/>
                            <span className={button.button + ' ' + start.marg} data-mod='2' onClick={gen_pas}>
                                <img src={ran} className={start.randimg} alt=""/>
                                Случайный пароль
                            </span>
                        </div>
                        <div className={start.blockRec} data-selemail={+selEmailR} ref={el=>elem.blockRecR=el}>
                            <div className={start.email}>
                                <div className={start.raz}>
                                    E-Mail:
                                    <span style={{color: "#F00"}}> *</span>
                                </div>
                                <div className={start.dinp}>
                                    <input className={start.inps} ref={el => elem.emalR = el} type="email" placeholder="E-Mail" onChange={chStatRb} id="emalR" required/>
                                    <span className={button.button + ' ' + start.marg} data-mod='2' onClick={changeSelEmailR}>
                                        Заменить на секретную фразу
                                    </span>
                                </div>
                            </div>
                            <div className={start.secFR}>
                                <div className={start.raz}>
                                    Секретная фраза:
                                    <span style={{color: "#F00"}}> *</span>
                                </div>
                                <div className={start.dinp}>
                                    <input className={start.inps} type="text" placeholder="Секретная фраза" onChange={chStatRb} id="secFrR"
                                           required pattern="^[a-zA-Z0-9_]+$"/>
                                    <span className={button.button + ' ' + start.marg} data-mod='2' onClick={changeSelEmailR}>
                                        Заменить на e-mail
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div className={start.dinp}>
                            <div className={start.dinpo}>
                                <div className={start.lic}>
                                    <CheckBox text={"Принимаю условия "} checkbox_id={"checkbox_lic"}/>
                                    <span className={start.url}
                                          onClick={() => dispatch(changeDialog(CHANGE_DIALOG, licField))}>
                                        соглашения
                                    </span>
                                    <span style={{color: "#F00"}}> *</span>
                                    <div>
                                        <span style={{color: "#F00"}}>*</span>
                                         — поля, обязательные для заполнения
                                    </div>
                                </div>
                                <div className={start.warn} id="warncr">
                                    <img src={warn} className={start.warnimg} alt=""/>
                                    Включён Caps Lock!
                                </div>
                            </div>
                            <span data-enable={+els.regb} className={button.button + ' ' + start.marg}
                                 ref={el => elem.regbut = el} onClick={preRego}>
                                ЗАРЕГИСТРИРОВАТЬСЯ!
                            </span>
                        </div>
                    </form>
                </div>
            </div>
        }
    </div>
}
export default Start;

function getLic(){
    return (
        <pre style={{whiteSpace: "pre-wrap", wordBreak: "break-all", width: "95%"}}>
			<br/><strong>ЛИЦЕНЗИОННЫЙ ДОГОВОР</strong>
			<br/><strong>о предоставлении права на использование программного обеспечения на условиях простой (неисключительной) лицензии</strong>
			<br/>Настоящий лицензионный договор заключен между ООО "Умные решения" (далее — «Правообладатель»), в лице Генерального директора Лапина Сергея Владимировича, действующего на основании Устава, с одной стороны, и  _______________(далее - « Клиент») в лице ____________________ действующего на основании __________________________, с другой стороны, далее совместно именуемыми «Стороны». Стороны договорились о нижеследующем:
			<br/>
			<br/><strong>1.     ПРЕДМЕТ ДОГОВОРА</strong>
			<br/>Стороны договорились о нижеследующем:
			<br/>1.1. Правообладатель предоставляет Клиенту право на использование программного обеспечения «Система управления сайтами UMNI.RESH» на условиях простой (неисключительной) лицензии (далее - «Система») способами, указанными ниже. Состав и наименования редакций Систем, права на использование которых предоставляются (передаются) Клиенту, указывается в счёте, выставляемом Правообладателем на основании предварительной заявки Клиента, а также в Актах передачи прав.
			<br/>1.2. За предоставляемые по настоящему Договору права Клиент обязуется уплачивать Правообладателю вознаграждение, размер которого определяется в соответствии с действующим ценами Правообладателя, размещёнными на странице сайта Правообладателя в сети Интернет <a href="http://www.umni-resh.ru/editions/">http://www.umni-resh.ru/editions/</a>, и указывается в счёте, выставляемом Правообладателем на основании предварительной заявки Клиента.
			<br/>1.3. Клиенту предоставляются права на использование Системы следующими способами:
			<ul>
				<li>* право на воспроизведение на Хостинговой площадке с целью инсталляции, копирования и запуска в соответствии с сопроводительной документацией;</li>
				<li>* право на переработку - в той степени, в которой эта возможность описана в  документации к Системе;</li>
				<li>* право на доведение до всеобщего сведения - в той степени, в которой это необходимо для функционирования Сайта, созданного на основании  Системы.</li>
			</ul>
			<br/>
			<br/><strong>2. ОСНОВНЫЕ ПОНЯТИЯ, ИСПОЛЬЗУЕМЫЕ В ДОГОВОРЕ</strong><span style={{fontSize: "12px"}}> </span>
			<br/>2.1. «Система управления сайтами UMNI.RESH» - программное обеспечение для управления сайтами в сети Интернет. Обеспечивает создание сайта, управление информацией, дизайном, регистрацию пользователей. Администраторы сайта при помощи веб-интерфейса могут изменять содержимое сайта, его настройки и оформление, получать отчёты о работе сайта и сообщения, переданные пользователями. Функциональность UMNI.RESH может быть расширена путём установки дополнительных модулей.
			<br/>2.2. Клиент — физическое или юридическое лицо, приобретающее право использования Системы в пределах, установленных настоящим договором и редакцией Системы, условия использования которых размещены на странице в сети Интернет <a href="http://www.umni-resh.ru/editions/">http://www.umni-resh.ru/editions/</a>, для создания собственного сайта.
			<br/>2.3. Лицензия (лицензионный договор) — соглашение между Правообладателем и Клиентом об использовании Системы Клиентом в пределах, установленных договором. По настоящему договору право использования Системы предоставляется Клиенту на условиях простой (неисключительной) лицензии с сохранением за Правообладателем права выдачи лицензий другим лицам.
			<br/>2.4. Лицензионный ключ - используется Правообладателем для инициализации, защиты и предотвращения Системы от незаконного использования, содержит информацию о редакции Системы, о расширениях Системы, о модулях Системы, о шаблонах дизайнов, используемых доменных именах и электронной почте Клиента, правомерно владеющего лицензией на использование Системы.
			<br/>2.5. Сайт в сети Интернет - совокупность экземпляра системы и иной информации, содержащейся в базе данных сайта, составляющая информационную систему, доступ к которой обеспечивается посредством сети Интернет по доменным именам и (или) по сетевым адресам, позволяющим идентифицировать сайты в сети Интернет.
			<br/>2.6. IP-адрес (сетевой адрес) — уникальный идентификатор оборудования в сети передачи данных, подключённого к сети Интернет, используемый в том числе для числовой идентификации пользователей и ресурсов в сети.
			<br/>2.7. Доменное имя — символьный идентификатор, предназначенный для адресации сайтов в сети Интернет в целях обеспечения доступа к информации, размещённой в сети Интернет.
			<br/>2.8. Владелец сайта в сети Интернет - лицо, самостоятельно и по своему усмотрению определяющее порядок использования сайта в сети Интернет, в том числе порядок размещения информации на таком сайте.
			<br/>2.9. Хостинговая площадка — аппаратно программный комплекс (сервер или группа серверов) для размещения и поддержания сайта в сети Интернет, принадлежащий Клиенту или предоставляемый третьей стороной (провайдером хостинга).
			<br/>2.10. Расширенная коммерческая лицензия (далее - РКЛ) — право использования обновлений (версий, новых релизов) Системы, направленных на улучшение Системы. Данный тип лицензии имеет срочный характер и предоставляет Клиенту возможность получать обновления Системы,  выпущенные Правообладателем в период действия РКЛ.
			<br/>2.11. Основной сайт Правообладателя — <a href="http://www.umniresh.ru/">http://www.umni-resh.ru</a>.
			<br/>
			<br/><strong>3. ПРАВА И ОБЯЗАННОСТИ СТОРОН</strong><span style={{fontSize: "12px"}}> </span>
			<br/>3.1. Права и обязанности Правообладателя.
			<br/>3.1.1. Правообладатель предоставляет Клиенту право использования экземпляра Системы и обновлений  к ней в течение одного первого года использования Системы на условиях 100% предоплаты указанного в счете вознаграждения. Право на получение обновлений начиная со второго года обусловлено приобретением  РКЛ.
			<br/>3.1.2. Правообладатель использует лицензионный ключ для инициализации, защиты и предотвращения Системы от незаконного использования.
			<br/>3.1.3. По одной лицензии Клиенту предоставляется право создать один собственный Сайт на базе одного экземпляра Системы. Создание большего числа Сайтов возможно только при условии приобретения дополнительных экземпляров Системы. Исключение из этого условия составляет только использование специальной функции Системы (наличие функции зависит от редакции), которая позволяет Клиенту на одной хостинговой площадке с использованием единого IP-адреса создавать и устанавливать неограниченное число сайтов с разными доменными именами исключительно для собственного использования. В этом случае для всех сайтов используется единая база данных.
			<br/>3.2. Права и обязанности Клиента.
			<br/>3.2.1. Клиент обладает правом создавать на основе приобретаемой лицензии и с использованием Системы сайты в сети Интернет в пределах, установленных настоящим договором и выбранной Клиентом редакцией Системы.
			<br/>3.2.2. Клиент обладает правом использовать логотипы Системы при проведении рекламных акций, размещении информации о Системе на своём сайте, с указанием на исключительные права Правообладателя на Систему.
			<br/>3.2.3. Профессиональные консультации по работе Системы предоставляются Службой Заботы Умные решения без взимания дополнительной оплаты в течении одного года с момента приобретения права использования Системы,  далее - при условии приобретения Клиентом РКЛ на соответствующий срок.
			<br/>3.2.4. Клиент вправе вносить изменения в правомерно приобретённый экземпляр Системы исключительно в целях функционирования Системы на конкретных технических средствах Клиента или под управлением конкретных программ Клиента (адаптация) в пределах, указанных на странице <a href="http://docs.umni-resh.ru/system_modification_rules/">http://docs.umni-resh.ru/system_modification_rules/</a>, а также исправлять явные ошибки Системы.
			<br/>
			<br/><strong>4. СОХРАНИЕ ПРАВ И СОБСТВЕННОСТИ</strong>
			<br/>4.1. Клиент может использовать Систему только в пределах тех прав и теми способами, которые предусмотрены настоящим договором. Право использования Системы, прямо не указанное в настоящем договоре, не считается предоставленным Клиенту.
			<br/>4.2. Исключительные права на Систему принадлежат Правообладателю.
			<br/>
			<br/><strong>5. ОГРАНИЧЕНИЕ НА ВСКРЫТИЕ ТЕХНОЛОГИИ И ДЕКОМПИЛЯЦИЮ</strong>
			<br/>5.1.Запрещается вскрывать технологию, расшифровывать, декодировать, производить обратный инжиниринг или декомпилировать систему и любые её компоненты (включая базу данных) за исключением случаев и только в той степени, когда такие действия явно разрешены законодательством, несмотря на наличие в лицензионном соглашении данного ограничения.
			<br/>
			<br/><strong>6. ПЕРЕДАЧА ПРОГРАММНОГО КОДА</strong><span style={{fontSize: "12px"}}> </span>
			<br/>6.1. Передача  Клиенту экземпляра Системы осуществляется путём скачивания с сайта Правообладателя посредством сети Интернет.
			<br/>6.2. Лицензионный ключ для активации Системы передаётся Клиенту путем электронной почты (email) в течении пяти рабочих дней с момента зачисления денежных средств на расчётный счет Правообладателя.
			<br/>6.3. Право на использование Системы считается предоставленным Клиенту с момента получения Клиентом лицензионных ключей от Правообладателя. Предоставление фиксируется в двустороннем Акте передачи прав.
			<br/>6.4. Правообладатель оформляет в двух экземплярах Акт приема-передачи и направляет их Клиенту, который обязан подписать полученные экземпляры Акта приёма-передачи и вернуть один экземпляр Акта Правообладателю в 5-тидневный срок с момента получения, либо в указанный срок представить Правообладателю мотивированные возражения против подписания Акта. В случае неполучения Правообладателем в установленный настоящим пунктом срок мотивированных возражений от Клиента, право использования экземпляров Системы, указанных в таком Акте, считаются предоставленными (переданными) Клиенту надлежащим образом и принятыми им в полном объёме без каких-либо возражений.
			<br/>6.5. Проверка наименования, конфигурации, иных данных, касающихся предоставляемых прав на использование экземпляров Системы, осуществляется Клиентом в период, предоставленных для подписания Акта приёма-передачи. В случае выявления каких-либо несоответствий Клиент направляет Правообладателю соответствующие возражения. В этом случае, моментом передачи прав считается дата урегулирования Правообладателем обоснованных возражений Клиента.
			<br/>6.6. Права на использование  [конкретной переданной редакции и версии] Системы предоставляются на весь срок действия исключительного права.  Предоставление  иных редакций и/или версий Системы может являться предметом иных [основанных на заказах Клиента] сделок сторон.
			<br/>6.7. Клиенту разрешается переносить Систему на другую хостинговую площадку, уведомив Правообладателя в срок не менее, чем за 1 рабочий день.
			<br/>6.8. После переноса Системы на другую хостинговую площадку её следует полностью удалить с исходной хостинговой площадки. Одновременное использование одного экземпляра Системы на различных хостинговых площадках не допускается.
			<br/>6.9. В случае передачи Клиентом Системы (в том числе в составе сайта в сети Интернет, принадлежащего Клиенту) третьим лицам необходимо уведомить Правообладателя в письменном виде с указанием реквизитов нового владельца (наименование и ИНН юридического лица либо ФИО физического лица, e-mail) и приложением заверенной Клиентом копии договора, на основании которого производится такая передача. После этого уведомления, использование Системы Клиентом запрещено.  Новый владелец экземпляра Системы (сайта в сети Интернет, ранее принадлежащего Клиенту) вправе использовать экземпляр Системы в пределах прав, предоставленных настоящим договором.
			<br/>
			<br/><strong>7. РАСТОРЖЕНИЕ ДОГОВОРА</strong>
			<br/>7.1.Без ущерба для каких-либо иных своих прав Правообладатель вправе прекратить действие настоящего договора при несоблюдении Клиентом его положений и условий  путем направления уведомления об одностороннем внесудебном отказе от исполнения договора. При прекращении действия лицензионного договора Клиент обязан уничтожить все имеющиеся у него копии Системы и все её составные части.
			<br/>
			<br/><strong>8. ПОЛНОТА ДОГОВОРА. СТЕПЕНЬ ДЕЙСТВИЯ</strong>
			<br/>8.1.Настоящий договор (включает любые документы, дополняющие или изменяющие настоящий лицензионный договор, сопровождающий систему) составляет полное соглашение между Клиентом и Правообладателем относительно использования системы и заменяет собой все предшествующие или одновременные устные или письменные договоренности, предложения и заверения относительно системы и любых других положений настоящего лицензионного договора. Если какое-либо положение настоящего договора будет признано аннулированным, недействительным, не имеющим юридической силы или незаконным, то остальные положения настоящего лицензионного соглашения сохраняют свою полную силу и действие.
			<br/>8.2.При противоречии условий настоящего договора и условий какого-либо партнерского договора о системе, заключенного между Правообладателем и Клиентом, преимущественное значение имеют условия такого партнерского договора, во всем остальном применяются условия и настоящего соглашения, и такого договора.
			<br/>
			<br/><strong>9. ГАРАНТИЯ</strong>
			<br/>9.1.Система предназначается и предоставляется в качестве системы управления сайтом, в состоянии «как есть» со всеми недостатками, которые она может иметь на момент предоставления. Вы соглашаетесь с тем, что никакая система не свободна от ошибок. При условии наличия у вас действительной лицензии, Правообладатель гарантирует, что:
			<br/>9.1.1. В течение 90 дней с даты получения лицензии на использование системы либо в течение наименьшего срока, допускаемого законодательством, функционирование системы будет в основном соответствовать сопровождающим продукт документам. В случае выявления попыток изменения кода или попыток иного несанкционированного вмешательства в систему, гарантийные и любые иные обязательства Правообладателя аннулируются.
			<br/>9.1.2. Система будет в основном соответствовать описанию, содержащемуся в соответствующих документах, предоставляемых Клиенту Правообладателем, и инженеры по технической поддержке Правообладателя приложат все разумные усилия, проявят разумную заботу и применят профессиональные навыки для разрешения проблемных вопросов. Если система не соответствует настоящей гарантии, Правообладатель, либо осуществит исправление или замену системы, либо вернет сумму уплаченного вознаграждения (если оно перечислялось непосредственно  Правообладателю).
			<br/>9.2.Настоящая гарантия недействительна, если сбой в работе системы возник в результате неосторожности, неправильного обращения или применения. В случае замены, в отношении любого заменяющего продукта гарантия будет действовать в течение периода, оставшегося от изначального гарантийного срока, или в течение 30 дней, в зависимости от того, какой из указанных периодов будет больше. Клиент соглашается с тем, что вышеуказанная гарантия является единственной имеющейся у Клиента гарантией в отношении системы и любых услуг по технической поддержке.
			<br/>9.3.Любое обслуживание проданной системы, в том числе гарантийное, а так же консультирование, устранение неисправностей, техническая поддержка, любая иная помощь Правообладателя, предусмотренная условиями настоящего договора, оказывается, по адресам электронной почты и реквизитам, указанным в настоящем соглашении в соответствии с правилами Правообладателя
			<br/>9.4.Правообладатель не предоставляет на время соответствующих работ (согласно предыдущему пункту) какую-либо временную замену программного обеспечения, либо компьютера.
			<br/>9.5.Правообладатель не производит гарантийных работ, если такие работы обусловлены неудовлетворительной работой программы с иным программным обеспечением, установленным и используемым на компьютере незаконно, без соответствующих и необходимых в соответствии с законом лицензий и разрешений, если компьютер неисправен, либо не соответствует минимальным требованиям, предъявляемым программой к компьютеру.
			<br/>9.6.Правообладатель не несет ответственности за работу программы и отказывает в её гарантийном обслуживании, если она была каким-либо образом изменена (изменены качества, свойства, функции, назначение, структура), способами, не предусмотренными в документации к Системе, а так же, если она была повреждена иным программным обеспечением, в силу свойств такого программного обеспечения, в случае несоответствия аппаратного обеспечения техническим условиям предъявляемым продуктом, а также, если программа была повреждена компьютерным вирусом, иной вредоносной программой, либо повреждена Клиентом или третьими лицами умышленно, равно как и по неосторожности.
			<br/>9.7.Клиент может изменять, добавлять или удалять любые файлы приобретенной программы (включая базу данных) в соответствие с Российским Законодательством об авторском праве. В этом случае Правообладатель не гарантирует бесперебойную работу программы и обновлений.
			<br/>
			<br/><strong>10. ОГРАНИЧЕНИЕ ОТВЕТСТВЕННОСТИ</strong>
			<br/>10.1. В максимальной степени, допускаемой законодательством и за исключением случаев, прямо предусмотренных условиями настоящего договора, Правообладатель не несет ответственность за какие-либо убытки и/или ущерб (в том числе, убытки в связи недополученной коммерческой выгодой, прерыванием коммерческой и производственной деятельности, утратой данных), возникающие в связи с использованием или невозможностью использования Системы. В любом случае ответственность Правообладателя ограничивается суммой, фактически уплаченной Клиентом Правообладателю за пользование Системой. Настоящие ограничения не применяются в отношении тех видов ответственности, которые не могут быть исключены или ограничены в соответствии с законом.
			<br/>10.2. Правообладатель не несёт ответственности ни при каких обстоятельствах за любую упущенную выгоду, ущерб, моральный ущерб, убытки и вред, причинённый кому бы тони было в результате использования Системы, утраты информации и прочего, если не будет доказан умысел Правообладателя в причинении вышеуказанных последствий.
			<br/>10.3. Правообладатель не несёт ответственности ни при каких обстоятельствах за содержание информации, размещаемой Клиентом в Системе. Клиент самостоятельно в полной мере несет ответственность, в том числе перед третьим лицами, за размещаемую и/или передаваемую им информацию с использованием Системы.
			<br/>10.4. При невозможности разрешить спор или претензии, мирным путём, Стороны договорились о подсудности разрешения спора с участием Правообладателя по месту нахождения Правообладателя в порядке, предусмотренном действующим законодательством Российской Федерации.
			<br/>10.5. В случае возникновения у Клиентов вопросов, касающихся настоящего лицензионного договора или иных вопросов в зоне ответственности Правообладателя, следует использовать форму обращения на сайте <a href="http://www.umni-resh.ru/">http://www.umni-resh.ru</a>.
			<br/><strong>11.   РЕКВИЗИТЫ И ПОДПИСИ СТОРОН</strong>
			<br/><span>Правообладатель: </span><strong>ООО "Умные решения"</strong>
			<br/>ИНН/ КПП 7840000361 / 700001001
			<br/>БИК 000030778
			<br/>Р/с: 40702810000540000136
			<br/>в СЕВЕРО-ЗАПАДНЫЙ ФИЛИАЛ ПАО РОСБАНК г. Санкт-Петербург
			<br/>Корр. счет: 30100000100000000778
			<br/>Генеральный директор
			<br/>______________________ С.В. Лапин
		</pre>
    )
}