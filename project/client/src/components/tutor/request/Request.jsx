import React, {useEffect, useRef} from "react";
import requestCSS from './request.module.css';
import tutorCSS from '../tutor.module.css';
import {useDispatch, useSelector} from "react-redux";
import {states, themes} from "../../../store/selector";
import button from "../../button.module.css";
import {CHANGE_EVENT, changeEvents} from "../../../store/actions";
import {send} from "../../main/Main";

let dispatch, cState, inps;
inps = {};

function addReq(e) {
    send({
        text: inps.inpnnt_,
        title: inps.inpnet_,
        dat: new Date().toLocaleString("ru", {day:"2-digit", month: "2-digit", year:"numeric"})
    }, 'POST', "requests", "addReq")
        .then(data => {
            if(data.error == false){
                let title, text;
                title = "Внимание!";
                text = "Данные отправлены. В течении дня мы с вами свяжемся. До связи:3";
                dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
            }
        });
}

function chStatB(e) {
    let el = e.target;
    inps[el.id] = !el.validity.patternMismatch || !el.validity.typeMismatch ? el.value : false;
    if (inps[el.id]) {
        el.setAttribute("data-mod", '0');
    } else {
        el.setAttribute("data-mod", '1');
    }
    let but = el.parentElement.parentElement.querySelector("."+button.button);
    if(but) {
        but.setAttribute("data-enable", +(inps.inpnnt_ && inps.inpnet_));
    }
}

function ele (x, par) {
    if(!inps[par]) inps[par] = x;
}

export function Request() {
    const themeState = useSelector(themes);
    cState = useSelector(states);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Request.jsx");
        for(let el of document.querySelectorAll(" *[id^='inpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
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
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" req"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Заявка на подключение
            </div>
            <div className={tutorCSS.block}>
                Вы не имеете аккаунта и ваша школа ещё не подключена к нашей системе?
                <br/>Заполните форму ниже оставив ваши контактные данные и адрес электронной почты, мы с вами
                свяжемся.
                <div className={requestCSS.blockInp}>
                    <div className={requestCSS.preinf}>
                        ФИО:
                    </div>
                    <input className={requestCSS.inp} id={"inpnnt_"} placeholder={"Фамилия Имя Отчество"} onChange={chStatB} type="text"/>
                    {ele(false, "inpnnt_")}
                </div>
                <div className={requestCSS.blockInp}>
                    <div className={requestCSS.preinf}>
                        E-Mail:
                    </div>
                    <input className={requestCSS.inp} id={"inpnet_"} placeholder={"example@gmail.com"} onChange={chStatB} type="email"/>
                    {ele(false, "inpnet_")}
                </div>
                <div className={button.button} data-mod="2" onClick={addReq}>
                    Отправить!
                </div>
            </div>
        </div>
    )
}
export default Request;