import React, {useEffect, useRef} from "react";
import {useNavigate, useParams} from "react-router-dom"
import {Helmet} from "react-helmet-async";
import profileCSS from './profile.module.css';
import {profiles, states, themes} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import yes from "../../../media/yes.png";
import no from "../../../media/no.png";
import ed from "../../../media/edit.png";
import profd from "../../../media/profd.png";
import profl from "../../../media/profl.png";
import {
    CHANGE_EVENTS_CLEAR,
    CHANGE_PROFILE,
    CHANGE_PROFILE_GL,
    CHANGE_PROFILE_ROLES,
    CHANGE_STATE,
    changeEvents,
    changeProfile,
    changeState
} from "../../../store/actions";
import {addEvent, eventSource, prefSite, sendToServer, setActived} from "../Main";
import ErrFound from "../../other/error/ErrFound";
import ls1 from "../../../media/ls-icon1.png";
import ls2 from "../../../media/ls-icon2.png";
import ls3 from "../../../media/ls-icon3.png";
import {cProfiles} from "../../other/Controllers";

let profilesInfo, dispatch, moore, errText, cState, navigate, icons;
errText = "К сожалению, информация не найдена...";

moore = `/*
Можете что-то рассказать о себе
Дополнительные контакты:
	Телефон: 8 800 555 35 35
	ВК: https://vk.com/id
	Телеграмм: https://t.me/id
	e-mail1: fsdfdsfd@ya.ru
	e-mail2: fsdfdsfd2@ya.ru
*/`;

icons = {
    1: ls1,
    2: ls2,
    3: ls3
}

function inpchr(event){
    var el = event.target;
    if (el.validity.patternMismatch || el.validity.typeMismatch || el.value.length == 0) {
        el.setAttribute("data-mod", '1');
        // warner.style.display = "inline";
    } else {
        el.setAttribute("data-mod", '0');
        // warner.style.display = "none";
    }
    el.parentElement.querySelector(".yes").setAttribute("data-enable", +(!el.validity.typeMismatch && !el.validity.patternMismatch && el.value.length != 0));
}

function onEdit(e) {
    let par = e.target.parentElement;
    par.setAttribute('data-mod', '1');
}

function onFin(e) {
    let par, inp;
    par = e.target.parentElement;
    inp = par.querySelector("." + profileCSS.inp);
    if(inp.tagName == "TEXTAREA") {
        sendToServer({
            login: cState.login,
            info: inp.value,
            uuid: cState.uuid
        }, 'POST', cProfiles+"chInfo")
            .then(data => {
                if(data.error == false){
                    par.setAttribute('data-mod', '0');
                }
            });
    } else if (inp.validity.typeMismatch || inp.validity.patternMismatch || inp.value.length == 0) {
        inp.setAttribute("data-mod", '1');
        // warner.style.display = "inline";
    } else {
        inp.setAttribute("data-mod", '0');
        // warner.style.display = "none";
        if (inp.type == "email") {
            sendToServer({
                login: cState.login,
                email: inp.value,
                uuid: cState.uuid
            }, 'POST', cProfiles+"chEmail")
                .then(data => {
                    if(data.error == false){
                        par.setAttribute('data-mod', '0');
                    }
                });
        } else {
            sendToServer({
                oLogin: cState.login,
                nLogin: inp.value,
                uuid: cState.uuid
            }, 'POST', cProfiles+"chLogin")
                .then(data => {
                    if(data.error == false){
                        dispatch(changeState(CHANGE_STATE, "login", data.body.login));
                        par.setAttribute('data-mod', '0');
                        navigate(prefSite + "/profiles");
                    } else {
                        addEvent("Логин занят, попробуйте изменить", 10);
                    }
                });
        }
    }
}

function onClose(e) {
    let par = e.target.parentElement;
    par.setAttribute('data-mod', '0');
    // warner.style.display = "none";
}

function chInfo(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeProfile(CHANGE_PROFILE, "more", msg.body.more));
}

function chLogin(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeProfile(CHANGE_PROFILE, "login", msg.body.login));
}

function chEmail(e) {
    const msg = JSON.parse(e.data);
    dispatch(changeProfile(CHANGE_PROFILE_ROLES, "email", msg.body.email, msg.body.role));
}

function goToProf(log) {
    if(log) navigate(prefSite + "/profiles/" + log);
}

function onCon(e, log) {
    setInfo(log);
}

function setInfo(log) {
    sendToServer({
        login: log,
        uuid: cState.uuid
    }, 'POST', cProfiles+"getProfile")
        .then(data => {
            if(data.error == false){
                dispatch(changeProfile(CHANGE_PROFILE_GL, undefined, data.body));
            }
        });
}

export function Profile() {
    profilesInfo = useSelector(profiles);
    const { log } = useParams();
    navigate = useNavigate();
    cState = useSelector(states);
    const themeState = useSelector(themes);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        setActived(".panPro");
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        console.log("I was triggered during1 componentDidMount Profile.jsx");
        eventSource.addEventListener('connect', e=>onCon(e, log), false);
        eventSource.addEventListener('chEmail', chEmail, false);
        eventSource.addEventListener('chLogin', chLogin, false);
        eventSource.addEventListener('chInfo', chInfo, false);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            eventSource.removeEventListener('connect', e=>onCon(e, log));
            eventSource.removeEventListener('chLogin', chLogin);
            eventSource.removeEventListener('chInfo', chInfo);
            console.log("I was triggered during componentWillUnmount Profile.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(log && profilesInfo.login && log != profilesInfo.login) {
            setInfo(log);
        }
        console.log('componentDidUpdate Profile.jsx');
    });
    return <div className={profileCSS.AppHeader}>
        <Helmet>
            <title>Профиль</title>
        </Helmet>
        {!Object.getOwnPropertyNames(profilesInfo).length ?
                <ErrFound text={errText}/>
            :
                <div className={profileCSS.blockPro}>
                    <div className={profileCSS.pro}>
                        <img alt="ico" src={icons[profilesInfo.ico]}/>
                        <div className={profileCSS.nav_i} id={profileCSS.nav_i} data-mod='0'>
                            <div className={profileCSS.preinf}>
                                Логин:
                            </div>
                            <div className={profileCSS.field}>
                                {profilesInfo.login}
                            </div>
                            {(!log || log == cState.login) && <>
                                <input className={profileCSS.inp} id="loginp" placeholder="nickname" onInput={inpchr} defaultValue={profilesInfo.login} type="text" pattern="^[a-zA-Z0-9]+$"/>
                                <img className={profileCSS.imginp+" yes"} src={yes} onClick={onFin} title="Подтвердить изменения" alt=""/>
                                <img className={profileCSS.imginp} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                <img className={profileCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                            </>}
                        </div>
                        <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                            ФИО: {profilesInfo.fio}
                        </div>
                        <div className={profileCSS.nav_i} id={profileCSS.nav_i} data-mod='0'>
                            <div className={profileCSS.preinf}>
                                Дополнительная информация:
                            </div>
                            <pre className={profileCSS.field}>
                                {profilesInfo.more}
                            </pre>
                            {(!log || log == cState.login) && <>
                                <textarea className={profileCSS.inp+" "+profileCSS.inparea} placeholder="Информация о вас" onInput={inpchr} defaultValue={profilesInfo.more ? profilesInfo.more : moore}/>
                                <img className={profileCSS.imginp+" yes"} src={yes} onClick={onFin} title="Подтвердить изменения" alt=""/>
                                <img className={profileCSS.imginp} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                <img className={profileCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                            </>}
                        </div>
                        {profilesInfo.roles && Object.getOwnPropertyNames(profilesInfo.roles).map((param, i, x, role = profilesInfo.roles[param]) =>
                            <div className={profileCSS.nav_iZag} key={param}>
                                <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                                    Роль: {cState.rolesDescrs[param]}
                                </div>
                                {role.yo && <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                                    Учебная организация: {role.yo}
                                </div>}
                                <div className={profileCSS.nav_i} id={profileCSS.nav_i} data-mod='0'>
                                    <div className={profileCSS.preinf}>
                                        Почта:
                                    </div>
                                    <div className={profileCSS.field}>
                                        {role.email}
                                    </div>
                                    {(!log || log == cState.login) && <>
                                        <input className={profileCSS.inp} onInput={inpchr} placeholder="ex@gmail.com" defaultValue={role.email} type="email"/>
                                        <img className={profileCSS.imginp+" yes"} src={yes} onClick={onFin} title="Подтвердить изменения" alt=""/>
                                        <img className={profileCSS.imginp} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                        <img className={profileCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                    </>}
                                </div>
                                {role.group && <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                                    Класс: {role.group}
                                </div>}
                                {role.parents && <>
                                    <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                                        Родители:
                                    </div>
                                    <div className={profileCSS.nav_iZag}>
                                        {Object.getOwnPropertyNames(role.parents).map(param1 => <div key={param1}>
                                            <div className={profileCSS.nav_i+" "+profileCSS.preinf} id={profileCSS.nav_i}>
                                                {role.parents[param1].name}
                                            </div>
                                            <img className={profileCSS.proImg} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(role.parents[param1].login)} title="Перейти в профиль" alt=""/>
                                        </div>)}
                                    </div>
                                </>}
                                {role.kids && <>
                                    <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                                        Дети:
                                    </div>
                                    <div className={profileCSS.nav_iZag}>
                                        {Object.getOwnPropertyNames(role.kids).map(param1 => <div key={param1}>
                                            <div className={profileCSS.nav_i+" "+profileCSS.preinf} id={profileCSS.nav_i}>
                                                {role.kids[param1].name}
                                            </div>
                                            <img className={profileCSS.proImg} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(role.kids[param1].login)} title="Перейти в профиль" alt=""/>
                                        </div>)}
                                    </div>
                                </>}
                                {role.lessons && <>
                                    <div className={profileCSS.nav_i} id={profileCSS.nav_i}>
                                        Дисциплины:
                                    </div>
                                    <div className={profileCSS.nav_iZag}>
                                        {role.lessons.map(param1 =>
                                            <div className={profileCSS.nav_i} id={profileCSS.nav_i} key={param1}>
                                                {param1}
                                            </div>
                                        )}
                                    </div>
                                </>}
                            </div>
                        )}
                    </div>
                </div>
        }
    </div>
}
export default Profile;