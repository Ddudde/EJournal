import React, {useEffect, useRef} from "react";
import mainCSS from './main.module.css';
import {Link, Outlet} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {pane, states, themes} from "../../store/selector";
import {
    CHANGE_DIALOG,
    CHANGE_DIALOG_DEL,
    CHANGE_EVENT,
    CHANGE_EVENT_DEL,
    CHANGE_PANE_GR,
    CHANGE_STATE,
    CHANGE_STATE_GL,
    CHANGE_STATE_RESET,
    changeDialog,
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
import {setSettings} from "./settings/Settings";
import ls1 from "../../media/ls-icon1.png";
import ls2 from "../../media/ls-icon2.png";
import ls3 from "../../media/ls-icon3.png";

import {cAuth, cProfiles} from "../other/Controllers";

let act, ke, gr, cState, dispatch, paneInfo, themeInfo, scrolling,
timid, d1, warnErrNet, server;
scrolling = false;
// eslint-disable-next-line no-undef
server = servLink;
export let prefSite = "/DipvLom";
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
    let icons = {
        1: ls1,
        2: ls2,
        3: ls3
    };
    return <div className={mainCSS.logBlock}>
        <div className={mainCSS.nav_i+' '+mainCSS.log} style={{width:"100%"}} id={mainCSS.nav_i}>
            <img alt="ico" src={icons[cState.ico]}/>
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
}

function selKid(kid) {
    sendToServer({
        idL: kid
    }, 'PATCH', cProfiles+"chKid")
        .then(data => {
            if(data.status == 200) {
                console.log(data);
                dispatch(changeState(CHANGE_STATE, "kid", data.body.kid));
            }
        });
}

function getKids() {
    return cState.kids && <div className={mainCSS.logBlock}>
        <div className={mainCSS.nav_i+' '+mainCSS.kidEl} id={mainCSS.nav_i}>
            <img className={mainCSS.kidImg} src={themeInfo.theme_ch ? profd : profl} title="Перейти в профиль" alt=""/>
            <div className={mainCSS.kidInf}>Информация о:</div>
            <div className={mainCSS.kidText}>{cState.kids[cState.kid]}</div>
            <img className={mainCSS.mapImg} src={themeInfo.theme_ch ? mapd : mapl} title="Перейти в профиль" alt=""/>
        </div>
        <div className={mainCSS.logMenu}>
            {Object.getOwnPropertyNames(cState.kids).map(param1 =>
                <div className={mainCSS.nav_i+' '+mainCSS.log+' '+mainCSS.kidBlock} id={mainCSS.nav_i} onClick={e => selKid(param1)}>
                    <img className={mainCSS.kidImg} src={themeInfo.theme_ch ? profd : profl} title="Перейти в профиль" alt=""/>
                    <div className={mainCSS.kidInf}>Информация о:</div>
                    <div className={mainCSS.kidText}>{cState.kids[param1]}</div>
                </div>
            )}
        </div>
    </div>
}

export function sendToServer(bod, typeC, url) {
    // eslint-disable-next-line no-undef
    return sendToServerApp(bod, typeC, url);
}

function chRoles() {
    sendToServer(0, 'PATCH', cProfiles+"chRole")
        .then(data => {
            if(data.status == 200 && data.body.role != undefined){
                dispatch(changeState(CHANGE_STATE_GL, undefined, data.body));
            }
        });
}

function onExit() {
    let exitField = {
        obj: <div>
            Вы желаете выйти?
        </div>,
        buts: {
            0 : {
                text: "Да",
                fun: () => {
                    dispatch(changeState(CHANGE_STATE_RESET));
                    sendToServer({
                        notifToken: localStorage.getItem("notifToken")
                    }, 'PATCH', cProfiles+"exit");
                    dispatch(changeDialog(CHANGE_DIALOG_DEL))
                },
                enab: true
            },
            1 : {
                text: "Нет",
                fun:() => dispatch(changeDialog(CHANGE_DIALOG_DEL)),
                enab: true
            }
        }
    };
    dispatch(changeDialog(CHANGE_DIALOG, exitField))
}

export function setActived(name) {
    if(document.querySelector(act)) {
        document.querySelector(act).setAttribute('data-act', '0');
    }
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
    if(document.body.hasAttribute(themeInfo.thP[stat].p)) {
        document.body.removeAttribute(themeInfo.thP[stat].p)
    }
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

function iniNet() {
    eventSource = new EventSource(server + '/' + cAuth + 'start/' + localStorage.getItem("sec"));
    eventSource.onopen = e => console.log('open');
    eventSource.onerror = e => {
        if (e.readyState == EventSource.CLOSED) {
            console.log('close');
            closeStream();
        } else {
            console.log('try to reconnect....');
        }
        if(warnErrNet == undefined) {
            warnErrNet = addEvent("Отсутствует подключение к серверу", undefined, true);
        }
    };
    eventSource.addEventListener('chck', e => {
        const msg = JSON.parse(e.data);
        console.log(msg);
        if (msg) {
            localStorage.setItem("sec", msg);
            dispatch(changeState(CHANGE_STATE, "uuid", msg));
        }
        if(warnErrNet != undefined){
            remEvent(warnErrNet);
            warnErrNet = undefined;
        }
        sendToServer({
            login: cState.login,
            notifToken: localStorage.getItem("notifToken"),
            permis: Notification.permission == "granted"
        }, 'PATCH', cAuth+"infCon")
            .then(data => {
                if(data.status == 200) {
                    dispatch(changeState(CHANGE_STATE_GL, undefined, data.body));
                    // eslint-disable-next-line no-undef
                    appApp.dispatchEvent(new Event("op"));
                    if(cState.login) setSettings(dispatch);
                    eventSource.dispatchEvent(new Event("connect"));
                }
            });
    }, false);
}

function closeStream() {
    if(!eventSource) return;
    if(eventSource.readyState != EventSource.CLOSED) {
        sendToServer(0, 'PATCH', cAuth+"remCon");
        eventSource.close();
        // dispatch(changeState(CHANGE_STATE, "uuid", undefined));
    }
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
        console.log("I was triggered during componentWillUnmount Main.jsx")
    };
    gr.groups = {
        0: !cState.auth ? {
            nam: "Главная",
            linke: prefSite + "/"
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
            linke: prefSite + "/"
        } : undefined,
        9: cState.auth && cState.role == 2 ? {
            nam: "Журнал",
            linke: "journal"
        } : undefined,
        10: cState.auth && cState.role == 3 ? {
            nam: "Администрирование УО",
            linke: prefSite + "/"
        } : undefined,
        11: cState.auth && cState.role == 4 ? {
            nam: "Заявки",
            linke: prefSite + "/"
        } : undefined,
        12: cState.auth && cState.role == 4 ? {
            nam: "Тестирование",
            linke: "test"
        } : undefined,
        13: cState.auth && cState.role < 2 ? {
            nam: "Дневник",
            linke: prefSite + "/"
        } : undefined,
        14: cState.auth && cState.role < 2 ? {
            nam: "Аналитика",
            linke: "analytics"
        } : undefined
    };
    useEffect(() => {
        console.log("I was triggered during componentDidMount Main.jsx");
        scr();
        iniTheme(themeInfo.theme_ch);
        window.onpagehide = unMount;
        window.onbeforeunload = unMount;
        window.onscroll = () => {
            if(!scrolling) {
                scrolling = true;
                timid = setTimeout(tim,300);
            }
        };
        return unMount;
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Main.jsx');
    });
    return <>
        <div className={mainCSS.fon}>
            <div/>
            <div/>
        </div>
        <nav className={mainCSS.panel} id="her">
            <div className={mainCSS.pane} ref={()=>ke = ke || paneInfo.els.length}>
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
            © 2023 ООО "Рога и Копыта" Все права защищены. Project on <a href="https://github.com/Ddudde/DipvLom" style={{color: "var(--cV2)"}}>github</a>.
        </div>
        <img className={mainCSS.d1} src={up} title="Вверх" alt="" onClick={onTop} ref={el=>d1 = el}/>
    </>
}
export default Main;