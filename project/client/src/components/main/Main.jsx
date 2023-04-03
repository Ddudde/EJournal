import React, {useEffect, useRef} from "react";
import mainCSS from './main.module.css';
import {Link, Outlet} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {pane, states, themes} from "../../store/selector";
import {
    CHANGE_EVENT,
    CHANGE_EVENT_DEL,
    CHANGE_PANE_GR,
    CHANGE_STATE,
    CHANGE_STATE_GL,
    CHANGE_STATE_RESET,
    changeEvents,
    changeGroups,
    changeState,
    changeTheme
} from "../../store/actions";
import profd from "../../media/profd.png";
import profl from "../../media/profl.png";
import mapd from "../../media/Map_symbolD.png";
import mapl from "../../media/Map_symbolL.png";
import up from "../../media/up.png";
import Events from "../other/events/Events";
import Dialog from "../other/dialog/Dialog";
import Pane from "../other/pane/Pane";

let act, ke, gr, cState, dispatch, paneInfo, themeInfo, scrolling, timid, timidP, d1, warnErrNet;
scrolling = false;
gr = {
    group: 4
};

export let eventSource;

function getPan(name, namecl, link, dopClass, fun) {
    let cl = "pan" + namecl;
    return fun ?
        <div className={mainCSS.nav_i+" "+cl+" "+(dopClass ? dopClass : "")} id={mainCSS.nav_i} onClick={fun}>
            {name}
        </div>
    :
        <Link className={mainCSS.nav_i+" "+cl+" "+(dopClass ? dopClass : "")} id={mainCSS.nav_i} to={link} onClick={() => {setActived("."+cl)}}>
            {name}
        </Link>;
}

function getLogin() {
    return (
        <div className={mainCSS.logBlock}>
            <div className={mainCSS.nav_i+' '+mainCSS.log} style={{width:"100%"}} id={mainCSS.nav_i}>
                <img alt="ico" src={'/media/ls-icon'+ cState.ico +'.png'}/>
                <div className={mainCSS.logLog}>{cState.login}</div>
                <div className={mainCSS.logText}>Я - {cState.roleDesc}</div>
            </div>
            <div className={mainCSS.logMenu}>
                {getPan("Профиль", "Pro", "profiles", mainCSS.logMenuBlock)}
                {cState.roles && getPan("Сменить роль", "Rol", "", mainCSS.logMenuBlock,chRoles)}
                {getPan("Настройки", "Set", "settings", mainCSS.logMenuBlock)}
                {getPan("Выход", "Exi", "", mainCSS.logMenuBlock,onExit)}
            </div>
        </div>
    )
}

function getKids() {
    return cState.kids &&
        <div className={mainCSS.logBlock}>
            <div className={mainCSS.nav_i+' '+mainCSS.kidEl} id={mainCSS.nav_i}>
                <img className={mainCSS.kidImg} src={themeInfo.theme_ch ? profd : profl} title="Перейти в профиль" alt=""/>
                <div className={mainCSS.kidInf}>Информация о:</div>
                <div className={mainCSS.kidText}>{cState.kids[cState.kid]}</div>
                <img className={mainCSS.mapImg} src={themeInfo.theme_ch ? mapd : mapl} title="Перейти в профиль" alt=""/>
            </div>
            <div className={mainCSS.logMenu}>
                {Object.getOwnPropertyNames(cState.kids).map(param1 =>
                    <div className={mainCSS.nav_i+' '+mainCSS.log+' '+mainCSS.kidBlock} id={mainCSS.nav_i} onClick={() => (dispatch(changeState(CHANGE_STATE, "kid", param1)))}>
                        <img className={mainCSS.kidImg} src={themeInfo.theme_ch ? profd : profl} title="Перейти в профиль" alt=""/>
                        <div className={mainCSS.kidInf}>Информация о:</div>
                        <div className={mainCSS.kidText}>{cState.kids[param1]}</div>
                    </div>
                )}
            </div>
        </div>
}

export function send(bod, typeC, url, type) {
    let sed = {method: typeC};
    if(bod){
        sed.headers = {'Content-Type': 'application/json'};
        sed.body = JSON.stringify({
            type: type,
            body: bod
        });
    }
    return fetch("http://192.168.1.66:8080/"+(url ? url : ""), sed)
        .then(res => {
            if (!res.ok) {
                throw new Error(
                    `This is an HTTP error: The status is ${res.status}`
                );
            }
            return res.json();
        })
        .catch(data => {
            return data;
        });
}

function chRoles() {
    send({
        login: cState.login,
        role: cState.role
    }, 'POST', "auth", "chRole")
        .then(data => {
            if(data.error == false && data.body.role != undefined){
                dispatch(changeState(CHANGE_STATE_GL, undefined, data.body));
            }
        });
}

function onExit() {
    dispatch(changeState(CHANGE_STATE_RESET));
    send({
        uuid: cState.uuid
    }, 'POST', "auth", "exit");
}

export function setActived(name) {
    if(document.querySelector(act)) document.querySelector(act).setAttribute('data-act', '0');
    if(typeof name != "number"){
        if(document.querySelector(name)) {
            act = name;
            document.querySelector(name).setAttribute('data-act', '1');
        }
    }
    if(ke != undefined && paneInfo.els[ke]) {
        dispatch(changeGroups(CHANGE_PANE_GR, ke, name, undefined, false));
    } else {
        gr.group = name;
    }
}

function iniTheme(stat) {
    document.body.setAttribute(themeInfo.thP[stat].c, '');
    if(document.body.hasAttribute(themeInfo.thP[stat].p)) document.body.removeAttribute(themeInfo.thP[stat].p)
    Object.getOwnPropertyNames(themeInfo.thP[stat].params).map(param =>
        document.documentElement.style.setProperty(param, themeInfo.thP[stat].params[param])
    );
}

function scr() {
    if (window.pageYOffset >= window.innerHeight * 0.5)
        d1.style.display = "block";
    else
        d1.style.display = "none";
}

function tim() {
    if (scrolling) {
        scrolling = false;
        scr();
    }
}

export function addEvent(text, time, cons) {
    let title = "Внимание!";
    return dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, time, cons)).payload.id;
}

export function remEvent(id) {
    dispatch(changeEvents(CHANGE_EVENT_DEL, undefined, id));
}

function badPing() {
    if(warnErrNet == undefined){
        warnErrNet = addEvent("Отсутствует подключение к серверу", undefined, true);
    }
}

function iniNet() {
    eventSource = new EventSource('http://192.168.1.66:8080/auth/open-stream');
    eventSource.onopen = e => {
        console.log('open');
    };
    eventSource.onerror = e => {
        if (e.readyState == EventSource.CLOSED) {
            console.log('close');
            closeStream();
        }
        else {
            console.log('try to reconnect...');
        }
    };
    eventSource.addEventListener('chck', e => {
        const msg = JSON.parse(e.data);
        console.log(msg);
        dispatch(changeState(CHANGE_STATE, "uuid", msg));
        if(cState.login){
            send({
                login: cState.login,
                uuid: msg
            }, 'POST', "auth", "infCon");
        }
    }, false);
    eventSource.addEventListener('ping', e => {
        if(warnErrNet != undefined){
            remEvent(warnErrNet);
            warnErrNet = undefined;
        }
        clearTimeout(timidP);
        timidP = setTimeout(badPing,15000);
    }, false);
}

function closeStream() {
    if(!eventSource) return;
    if(eventSource.readyState != EventSource.CLOSED) send({
        uuid: cState.uuid
    }, 'POST', "auth", "remCon")
        .then(data => {
            if(data.error == false){
                dispatch(changeState(CHANGE_STATE, "uuid", undefined));
            }
        });
    eventSource.close();
}

function openStream() {
    if (eventSource != undefined && eventSource.readyState != EventSource.CLOSED) {
        console.log("stream already opened");
        return;
    }

    iniNet();
}

function onTop(e){
    window.scroll({
        left: 0,
        top: 0,
        behavior: "smooth"
    });
}

export function Main() {
    dispatch = useDispatch();
    if(!themeInfo) openStream();
    themeInfo = useSelector(themes);
    cState = useSelector(states);
    paneInfo = useSelector(pane);
    const isFirstUpdate = useRef(true);
    const unMount = () => {
        window.onwheel = undefined;
        closeStream();
        clearTimeout(timid);
        themeInfo = undefined;
        clearInterval(timidP);
        console.log("I was triggered during componentWillUnmount Main.jsx")
    };
    gr.groups = {
        0: !cState.auth ? {
            nam: "Главная",
            linke: "/"
        } : undefined,
        1: {
            nam: "Объявления",
            linke: "news"
        },
        2: {
            nam: "Контакты",
            linke: "contacts"
        },
        3: {
            nam: "Люди",
            linke: "people"
        },
        4: !cState.auth || (cState.auth && cState.role == 3) ? {
            nam: "Школам",
            linke: "tutor/sch"
        } : undefined,
        5: !cState.auth || (cState.auth && cState.role == 2) ? {
            nam: "Педагогам",
            linke: "tutor/tea"
        } : undefined,
        6: !cState.auth || (cState.auth && cState.role == 1) ? {
            nam: "Родителям",
            linke: "tutor/par"
        } : undefined,
        7: !cState.auth || (cState.auth && cState.role == 0) ? {
            nam: "Обучающимся",
            linke: "tutor/kid"
        } : undefined,
        8: cState.auth && cState.role == 2 ? {
            nam: "Расписание",
            linke: "/"
        } : undefined,
        9: cState.auth && cState.role == 2 ? {
            nam: "Журнал",
            linke: "journal"
        } : undefined,
        10: cState.auth && cState.role == 3 ? {
            nam: "Администрирование УО",
            linke: "admYO"
        } : undefined,
        11: cState.auth && cState.role == 4 ? {
            nam: "Заявки",
            linke: "request"
        } : undefined,
        12: cState.auth && cState.role < 2 ? {
            nam: "Дневник",
            linke: "/"
        } : undefined,
        13: cState.auth && cState.role < 2 ? {
            nam: "Аналитика",
            linke: "analytics"
        } : undefined
    };
    useEffect(() => {
        console.log("I was triggered during componentDidMount Main.jsx");
        scr();
        iniTheme(themeInfo.theme_ch);
        window.onscroll = () => {
            if(!scrolling) {
                scrolling = true;
                timid = setTimeout(tim,300);
            }
        };
        timidP = setTimeout(badPing,15000);
        window.onpagehide = unMount;
        window.onbeforeunload = unMount;
        return unMount;
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Main.jsx');
    });
    return (
        <>
            <div className={mainCSS.fon}>
                <div/>
                <div/>
            </div>
            <nav className={mainCSS.panel} id="her">
                <div className={mainCSS.pane} ref={()=>ke = ke == undefined ? paneInfo.els.length : ke}>
                    <Pane gro={gr} main={true}/>
                </div>
                {cState.auth && getLogin()}
                {(cState.auth && cState.role == 1) && getKids()}
            </nav>
            <Outlet/>
            <Dialog/>
            <Events/>
            <div className={mainCSS.switcher}>
                <label className={mainCSS.switch}>
                    <input className={mainCSS.inp_sw} type="checkbox" checked={themeInfo.theme_ch ? "checked" : ""} onChange={() => {dispatch(changeTheme(themeInfo.theme_ch, themeInfo.thP))}}/>
                    <span className={mainCSS.slider}/>
                </label>
                <div className={mainCSS.lab_sw}>
                    Тема: {themeInfo.theme}
                </div>
            </div>
            <div className={mainCSS.d}>
                © 2023 ООО "Рога и Копыта" Все права защищены. Project on <a href="https://github.com/Ddudde/Kursach-RSCHIR" style={{color: "var(--cV2)"}}>github</a>.
            </div>
            <img className={mainCSS.d1} src={up} title="Вверх" alt="" onClick={onTop} ref={(el)=>d1 = el}/>
        </>
    )
}
export default Main;