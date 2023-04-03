import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import tutorCSS from './tutor.module.css';
import {useDispatch, useSelector} from "react-redux";
import {states, themes} from "../../store/selector";
import {useParams} from "react-router-dom"
import kizo1 from "../../media/tutor/kids/izo1.png";
import kizo2 from "../../media/tutor/kids/izo2.png";
import kizo3 from "../../media/tutor/kids/izo3.png";
import kizo4 from "../../media/tutor/kids/izo4.png";
import kizo5 from "../../media/tutor/kids/izo5.png";
import kizo6 from "../../media/tutor/kids/izo6.png";
import kizo7 from "../../media/tutor/kids/izo7.png";
import kizo8 from "../../media/tutor/kids/izo8.png";
import kizo9 from "../../media/tutor/kids/izo9.png";
import kizo10 from "../../media/tutor/kids/izo10.png";
import kizo11 from "../../media/tutor/kids/izo11.png";
import kizo12 from "../../media/tutor/kids/izo12.png";
import kizo13 from "../../media/tutor/kids/izo13.png";
import kizo14 from "../../media/tutor/kids/izo14.png";
import kizo15 from "../../media/tutor/kids/izo15.png";
import kizo16 from "../../media/tutor/kids/izo16.png";
import kizo17 from "../../media/tutor/kids/izo17.png";
import kizo18 from "../../media/tutor/kids/izo18.png";
import kizo19 from "../../media/tutor/kids/izo19.png";
import pizo1 from "../../media/tutor/parents/izo1.png";
import tizo1 from "../../media/tutor/teachers/tizo1.png";
import tizo2 from "../../media/tutor/teachers/tizo2.png";
import tizo3 from "../../media/tutor/teachers/tizo3.png";
import tizo4 from "../../media/tutor/teachers/tizo4.png";
import tizo5 from "../../media/tutor/teachers/tizo5.png";
import tizo6 from "../../media/tutor/teachers/tizo6.png";
import tizo7 from "../../media/tutor/teachers/tizo7.png";
import tizo8 from "../../media/tutor/teachers/tizo8.png";
import tizo9 from "../../media/tutor/teachers/tizo9.png";
import hizo1 from "../../media/tutor/hteachers/izo1.png";
import hizo2 from "../../media/tutor/hteachers/izo2.png";
import hizo3 from "../../media/tutor/hteachers/izo3.png";
import hizo4 from "../../media/tutor/hteachers/izo4.png";
import hizo5 from "../../media/tutor/hteachers/izo5.png";
import hizo6 from "../../media/tutor/hteachers/izo6.png";
import hizo7 from "../../media/tutor/hteachers/izo7.png";
import hizo8 from "../../media/tutor/hteachers/izo8.png";
import hizo9 from "../../media/tutor/hteachers/izo9.png";
import hizo10 from "../../media/tutor/hteachers/izo10.png";
import hizo11 from "../../media/tutor/hteachers/izo11.png";
import hizo12 from "../../media/tutor/hteachers/izo12.png";
import hizo13 from "../../media/tutor/hteachers/izo13.png";
import hizo14 from "../../media/tutor/hteachers/izo14.png";
import hizo15 from "../../media/tutor/hteachers/izo15.png";
import hizo16 from "../../media/tutor/hteachers/izo16.png";
import hizo17 from "../../media/tutor/hteachers/izo17.png";
import hizo18 from "../../media/tutor/hteachers/izo18.png";
import hizo19 from "../../media/tutor/hteachers/izo19.png";
import hizo20 from "../../media/tutor/hteachers/izo20.png";
import hizo21 from "../../media/tutor/hteachers/izo21.png";
import hizo22 from "../../media/tutor/hteachers/izo22.png";
import hizo23 from "../../media/tutor/hteachers/izo23.png";
import hizo24 from "../../media/tutor/hteachers/izo24.png";
import hizo25 from "../../media/tutor/hteachers/izo25.png";
import hizo26 from "../../media/tutor/hteachers/izo26.png";
import hizo27 from "../../media/tutor/hteachers/izo27.png";
import hizo28 from "../../media/tutor/hteachers/izo28.png";
import hizo29 from "../../media/tutor/hteachers/izo29.png";
import hizo30 from "../../media/tutor/hteachers/izo30.png";
import hizo31 from "../../media/tutor/hteachers/izo31.png";
import hizo32 from "../../media/tutor/hteachers/izo32.png";
import hizo33 from "../../media/tutor/hteachers/izo33.png";
import hizo34 from "../../media/tutor/hteachers/izo34.png";
import hizo35 from "../../media/tutor/hteachers/izo35.png";
import hizo36 from "../../media/tutor/hteachers/izo36.png";
import {eventSource, send, setActived} from "../main/Main";
import knopka from "../../media/dnevnik/knopka.png";
import Request from "./request/Request";

let dispatch, ev, timid, scrolling, cState, zag, endSod, CWSel, type;
scrolling = false;
zag = {
    "kid": {
        name: "Обучающимся",
        link: 7,
        role: 0
    },
    "par": {
        name: "Родителям",
        link: 6,
        role: 1
    },
    "tea": {
        name: "Педагогам",
        link: 5,
        role: 2
    },
    "sch": {
        name: "Школам",
        link: 4,
        role: 3
    }
};

function getAdminYO() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" admYO"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Администрирование УО
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Страница "Администрирование УО" разделена на 3 подстранички: "Расписание звонков" и "Периоды
                    обучения" и "Дисциплины".
                    <br/>Для администрации учебного заведения все разделы поддаются редактированию.
                    {getImg(hizo27, 'Страница "Расписание звонков" с ролью администратора УО')}
                    {getImg(hizo28, 'Пример страницы без роли администратора УО')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Расписание звонков
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Добавление/изменение интервалов и смен происходит примерно по одному принципу. Поэтому рассмотрим
                    только один пример. Также возможно удалять как интервалы, так и смены.
                    {getImg(hizo27, 'Расписание звонков')}
                    Для изменения необходимо нажать на иконку с карандашём, в случае с добавлением, нажать на
                    соответствующую кнопку. После нажатия откроется меню редактирования, где можно ввести значение и
                    подтвердить изменение или же выйти из формы изменения/добавления поля.
                    <br/>Ограничение: недопустимы пустые поля.
                    {getImg(hizo29, 'Форма редактирования описания')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Периоды обучения
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Удалять возможно только период целиком.
                    {getImg(hizo30, 'Страница "Периоды обучения"')}
                    Для изменения необходимо нажать на иконку с карандашём. После нажатия откроется меню редактирования,
                    где можно ввести значение и подтвердить изменение или же выйти из формы изменения поля.
                    <br/>Ограничение: недопустимы пустые поля.
                    {getImg(hizo31, 'Форма редактирования названия учебного периода')}
                    В случае с добавлением, нажать на соответствующую кнопку. После нажатия откроется меню
                    редактирования, где можно ввести значение и подтвердить изменение или же выйти из формы
                    добавления поля.
                    <br/>Ограничение: необходимо заполнить все поля.
                    {getImg(hizo32, 'Форма добавления периода')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Дисциплины
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Удалять возможно только урок целиком.
                    {getImg(hizo33, 'Страница "Дисциплины"')}
                    Для изменения необходимо нажать на иконку с карандашём. После нажатия откроется меню редактирования,
                    где можно ввести значение и подтвердить изменение или же выйти из формы изменения поля.
                    <br/>Ограничение: недопустимы пустые поля.
                    {getImg(hizo34, 'Форма редактирования названия дисциплины')}
                    В случае с добавлением, нажать на соответствующую кнопку. После нажатия откроется меню
                    редактирования, где можно ввести значение и подтвердить изменение или же выйти из формы
                    добавления поля.
                    <br/>Ограничение: необходимо заполнить все поля.
                    {getImg(hizo35, 'Форма добавления урока')}
                    Выбор педагога возможен из существующих.
                    {getImg(hizo36, 'Выбор педагога к уроку')}
                </div>
            </div>
        </div>
    )
}

function getNews() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" news"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Объявления
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Страница "Объявления" разделена на 2 подстранички: "Объявления портала" и "Объявления учебной
                    организации".
                    <br/>У администрации учебного заведения имеется возможность редактировать информацию в
                    "Объявления учебной организации".
                    {getImg(hizo1, 'Страница "Объявления учебной организации" с ролью администратора УО')}
                    {getImg(hizo4, 'Пример страницы без роли администратора УО')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Добавление новости
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Для добавления новости необходимо нажать на соответствующую кнопочку.
                    <br/>Откроется шаблон для создания новости. В нём есть возможность изменения заголовка, текста,
                    даты и при необходимости изображения. Можно закрыть шаблон создания новости или подтвердить
                    публикацию новости из заполненного шаблона.
                    {getImg(hizo2, 'Появление шаблона для создания новости')}
                    Для изменения необходимо нажать на иконку с карандашём рядом с любой интересующей вас структурой.
                    После нажатия откроется меню редактирования, где можно ввести значение и подтвердить изменение или
                    же выйти из формы изменения поля.
                    <br/>Ограничение: недопустимы пустые поля.
                    {getImg(hizo3, 'Форма изменения поля')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Изменение новости
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Имеется возможность изменения заголовка, текста, даты и при необходимости изменения или удаления
                    изображения. Можно удалить новость.
                    {getImg(hizo1, 'Пример редактируемой новости')}
                    Для изменения необходимо нажать на иконку с карандашём рядом с любой интересующей вас структурой.
                    После нажатия откроется меню редактирования, где можно ввести значение и подтвердить изменение или
                    же выйти из формы изменения поля.
                    <br/>Ограничение: недопустимы пустые поля.
                    {getImg(hizo3, 'Форма изменения поля')}
                </div>
            </div>
        </div>
    )
}

function getContacts() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" cont"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Контакты
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Страница "Контакты" разделена на 2 подстранички: "Контакты портала" и "Контакты учебной
                    организации".
                    <br/>У администрации учебного заведения имеется возможность редактировать информацию в
                    "Контакты учебной организации".
                    {getImg(hizo5, 'Страница "Контакты учебной организации" с ролью администратора УО')}
                    {getImg(hizo6, 'Пример страницы без роли администратора УО')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Изменение контактов
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Имеется возможность изменения текста в обоих разделах и при необходимости измененить или удалить
                    изображение.
                    {getImg(hizo5, 'Пример изменяемых контактов')}
                    Для изменения необходимо нажать на иконку с карандашём рядом с любой интересующей вас структурой.
                    После нажатия откроется меню редактирования, где можно ввести значение и подтвердить изменение или
                    же выйти из формы изменения поля.
                    <br/>Ограничение: недопустимы пустые поля.
                    {getImg(hizo7, 'Форма редактирования описания')}
                </div>
            </div>
        </div>
    )
}

function getSettings() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" set"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Настройки
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Настройки доступны только авторизованным пользователям. На данной
                    страничке можно изменять настройки уведомлений, пароль, аватар,
                    секретную фразу.
                    {getImg(kizo19, 'Страница "Настройки"')}
                </div>
            </div>
        </div>
    )
}

function getProfil() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" prof"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Профиль
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    На этой страничке представленны все данные пользователя. Если вы
                    открыли свой профиль то у вас будет возможность дополнить/изменить
                    информацию.
                    {getImg(kizo18, 'Страница "Профиль"')}
                </div>
            </div>
        </div>
    )
}

function getZhur() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" jur"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Журнал
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Над оценками показываются даты.<br/>
                    Если оценка имеет вес более 1, то показывается рядом с оценкой.<br/>
                    Также если оценок много, то для удобства появляется возможность скроллить.<br/>
                    Для работы с журналом необходимо выбрать нужную оценку и тип. После чего,
                    нажать на интересующую клетку.
                    {getImg(tizo1, 'Страница "Журнал"')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Смена групп
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    При необходимости, можно переключаться между группами.<br/>
                    Не уместившиеся группы можно найти в меню-троеточии, наведя на него мышь.
                    {getImg(tizo2, 'Не уместившиеся группы')}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Смена предмета
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Можно переключаться между дисциплинами.<br/>
                    Наведя мышь, на текущую дисциплину, откроется меню переключения.
                    {getImg(tizo3, 'Иллюстрация меню переключения')}
                </div>
                <div className={tutorCSS.nav_i + " " + tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Выбор оценки
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    По умолчанию, включён режим "без оценки", чтобы обойтись без случайностей.<br/>
                    Переключение режимов происходит путём нажатия на интересующие кнопки.<br/>
                    Ограничение: "Н" нельзя ставить в итоговые оценки.
                    {getImg(tizo4, 'Выбор оценки')}
                </div>
                <div className={tutorCSS.nav_i + " " + tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Выбор типа оценки
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    По умолчанию, включён режим "ластик", он позволяет стереть описание оценки
                    и установить обычный вес оценки(1).<br/>
                    Переключение режимов происходит путём нажатия на интересующие кнопки.<br/>
                    Возможно удалить существущий тип оценки, при помощи нажатия на красный крест.<br/>
                    Ограничение: на "Н" нельзя устанавливать тип оценки, нельзя установить тип на итоговую оценку.
                    {getImg(tizo5, 'Выбор типа оценки')}
                    Также возможно изменить существующий тип оценки или добавить новый.<br/>
                    Нажатием на зелёную галочку, произойдёт подтверждение изменений. Если она
                    не активна, значит не соблюдается какое-то из ограничений.<br/>
                    Красный крест, отменит изменения и выключит режим редактирования.<br/>
                    Ограничение: в поле "тип" можно вписать только символы латиницы, кириллицы,
                    пробел и цифры, в поле "вес" можно вписать только цифры, поля должны быть
                    не пустыми.
                    {getImg(tizo6, 'Изменение типа оценки')}
                    {getImg(tizo7, 'Иллюстрация добавления типа оценки')}
                </div>
                <div className={tutorCSS.nav_i + " " + tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Домашние задания
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Возможно изменять или добавлять новые домашние задания.<br/>
                    Для этого необходимо нажать на синий карандаш в соответствующем поле.
                    {getImg(tizo8, 'Домашние задания')}
                    Функционал редактирования домашних заданий аналогичен редактированиию типов
                    оценок. В дополнение к нему, имеется поддержка многострочности.<br/>
                    Ограничение: поле должно быть не пустым.
                    {getImg(tizo9, 'Редактирование домашних заданий')}
                </div>
            </div>
        </div>
    )
}

function getAnalytic() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" ana"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Аналитика
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Журнал
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Над оценками показываются даты. Изначально показываются даты первой
                    дисциплины по списку. Чтобы показать числа выставления оценок других
                    дисциплин, необходимо навести мышь на линию интересующего вас предмета.<br/>
                    Если оценка имеет вес более 1, то показывается рядом с оценкой.<br/>
                    Если педагог уточнил тип оценки, то ниже журнала, появится соответствующая
                    запись, с датой и типом оценки.<br/>
                    Также если оценок много, то для удобства появляется возможность скроллить.
                    {getImg(kizo17, 'Страница "Журнал"')}
                </div>
            </div>
        </div>
    )
}

function getDnevnik() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" dnev"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Дневник
            </div>
            <div className={tutorCSS.block}>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Пользование
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Для того чтобы перейти к прошедшей неделе, необходимо проскроллить вверх, а
                    для перехода к следующей, сделать наоборот.<br/>
                    При наличии типа оценки, к примеру "Тест", можно навести мышь на оценку и
                    тип проявится.<br/>
                    При весе оценки больше одного, вес показывается.<br/>
                    Границы недель обозначены.<br/>
                    При достаточном отдалении от текущей недели появляется кнопка для
                    мгновенного
                    перехода к текущей неделе.
                    {getImg(kizo16, 'Страница "Дневник"')}
                </div>
            </div>
        </div>
    )
}

function getPeople() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" pep"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Люди
            </div>
            <div className={tutorCSS.block}>
                {zag[type].role == 3 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Пользование
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Страница "Люди" разделена на 5 подстраниц.
                        <br/>У администрации учебного заведения имеется возможность редактировать информацию в
                        "Педагоги", "Завучи", "Обучающиеся", "Родители".
                        {getImg(hizo8, 'Страница "Педагоги" с ролью администратора УО')}
                        {getImg(hizo9, 'Пример страницы c ролью ученика')}
                    </div>
                </>}
                {zag[type].role == 3 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Взаимодействие с ссылкой-приглашением
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Если у ссылки истёк срок и она была удалена или ещё не создана как в примере.
                        {getImg(hizo8, 'Не созданные ссылки')}
                        То необходимо нажать на значок обновления рядом с полем для ссылки.
                        {getImg(hizo13, 'Ссылка появилась')}
                        И ссылка появится в поле, откуда её можно скопировать при помощи выделения или сделать это при
                        помощи кнопки копирования, которая расположена рядом с кнопкой обновления.
                        {getImg(hizo14, 'Копирование ссылки при помощи кнопки')}
                        Срок действия ссылки - 30 дней.
                    </div>
                </>}
                {zag[type].role == 3 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Взаимодействие с группами
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        <div className={tutorCSS.block}>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Пользование
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Взаимодействие с группами можно встречать на разных страницах. О возможностях. Можно выбирать,
                                удалять или же переименовывать группы.
                                {getImg(hizo16, 'Пример страницы с группами')}
                                Если групп слишком много, то часть из них перемещается в меню с троеточием.
                                {getImg(hizo23, 'Меню с остальными группами')}
                                Выбирая группу из меню она переместится в "доступное" место.
                                {getImg(hizo24, 'Группа на видном месте')}
                            </div>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Добавление группы
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Для добавления группы необходимо нажать на соответствующую кнопочку.
                                <br/>Откроется шаблон для регистрации группы в системе. В нём есть возможность
                                изменения названия. Можно закрыть шаблон регистрирования или подтвердить создание группы.
                                <br/>Ограничение: недопустимы пустые поля, разрешены только латиница и цифры, а также
                                символы ".", "-".
                                {getImg(hizo25, 'Появление шаблона для добавления группы')}
                            </div>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Изменение группы
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Есть возможность удалить или изменить группу.
                                Для изменения необходимо нажать на иконку с карандашём. После нажатия откроется меню
                                редактирования, где можно ввести значение и подтвердить изменение или же выйти из формы
                                изменения поля.
                                <br/>Ограничение: недопустимы пустые поля, разрешены только латиница и цифры, а также
                                символы ".", "-".
                                {getImg(hizo26, 'Форма изменения названия')}
                            </div>
                        </div>
                    </div>
                </>}
                {zag[type].role == 3 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Педагоги
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Преподаватели делятся на "Нераспределённые педагоги" и "Педагоги" по принципу участия в
                        расписании учебного заведения.
                        <div className={tutorCSS.block}>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Добавление педагогов
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Для добавления педагогов необходимо нажать на соответствующую кнопочку.
                                <br/>Откроется шаблон для регистрации педагога в системе. В нём есть возможность
                                изменения ФИО. Можно закрыть шаблон регистрирования или подтвердить создание аккаунта.
                                {getImg(hizo10, 'Появление шаблона для добавления педагога')}
                                Для изменения необходимо нажать на иконку с карандашём. После нажатия откроется меню
                                редактирования, где можно ввести значение и подтвердить изменение или же выйти из формы
                                изменения поля.
                                <br/>Ограничение: недопустимы пустые поля.
                                {getImg(hizo11, 'Форма изменения поля')}
                            </div>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Внесение изменений в аккаунте педагога
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Имеется возможность изменения ФИО. Также можно удалить аккаунт.
                                {getImg(hizo8, 'Пример страницы с возможностью редактирования ФИО')}
                                Для изменения необходимо нажать на иконку с карандашём рядом с любой интересующей вас
                                структурой. После нажатия откроется меню редактирования, где можно ввести значение и
                                подтвердить изменение или же выйти из формы изменения поля.
                                <br/>Ограничение: недопустимы пустые поля.
                                {getImg(hizo12, 'Форма изменения ФИО')}
                            </div>
                        </div>
                    </div>
                </>}
                {zag[type].role == 3 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        "Завучи", "Обучающиеся"
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Управление аналогично странице "Педагоги", с единственным отличием, возможностью
                        управлять/выбирать нужную группу.
                        {getImg(hizo8, 'Страница "Педагоги"')}
                        {getImg(hizo15, 'Страница "Завучи"')}
                        {getImg(hizo16, 'Страница "Обучающиеся"')}
                    </div>
                </>}
                {zag[type].role == 3 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Родители
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        <div className={tutorCSS.block}>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Пользование
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Страница имеет название "Родители" условно, для узнаваемости. Можно регистрировать не только
                                родителей, но и любых представителей ученика.
                                {getImg(hizo17, 'Страница "Родители" с ролью администратора УО')}
                                {getImg(hizo18, 'Страница "Родители" с ролью ученика')}
                            </div><div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Добавление представителей ученику без представителей.
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Для возможности добавления представителей необходимо нажать на соответствующую кнопочку.
                                <br/>Откроется шаблон для регистрации представителей для ученика в системе. Для
                                регистрации необходимо выбрать ученика, в списке будут только ученики без
                                представителей. Можно закрыть шаблон регистрирования или подтвердить
                                создание аккаунтов.
                                Ограничение: подтверждение станет доступно только после добавление хотя бы одного
                                представителя.
                                {getImg(hizo19, 'Появление шаблона для выбора ученика')}
                                Далее необходимо добавить представителя. Это делается при помощи кнопки "Добавить
                                представителя". Можно закрыть шаблон регистрирования или подтвердить
                                создание аккаунта.
                                {getImg(hizo20, 'Появление шаблона для добавления представителя')}
                                Для изменения необходимо нажать на иконку с карандашём. После нажатия откроется меню
                                редактирования, где можно ввести значение и подтвердить изменение или же выйти из формы
                                изменения поля.
                                <br/>Ограничение: недопустимы пустые поля.
                                {getImg(hizo21, 'Форма изменения поля')}
                            </div>
                            <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                                Внесение изменений в представителях учеников
                            </div>
                            <div className={tutorCSS.blockOtv} data-act="0">
                                Имеется возможность изменения ФИО уже имеющихся представителей или добавить новых.
                                Принцип добавления такой же как и при регистрации ученику без представителей.
                                Также можно удалить аккаунты представителей.
                                {getImg(hizo17, 'Пример страницы с возможностью редактирования')}
                                Для изменения необходимо нажать на иконку с карандашём рядом с любой интересующей вас
                                структурой. После нажатия откроется меню редактирования, где можно ввести значение и
                                подтвердить изменение или же выйти из формы изменения поля.
                                <br/>Ограничение: недопустимы пустые поля.
                                {getImg(hizo22, 'Форма изменения ФИО')}
                            </div>
                        </div>
                    </div>
                </>}
                {zag[type].role < 2 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Педагоги
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        На данной странице учителя из списка "Мои педагоги" участвуют в вашем
                        обучении, остальные обозначены как "Другие педагоги".<br/>Нажав на иконку
                        профиля, можно перейти в профиль преподавателя.
                        {getImg(kizo11, 'Страница "Педагоги"')}
                    </div>
                </>}
                {zag[type].role < 2 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Педагоги
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        На данной странице учителя из списка "Мои педагоги" участвуют в вашем
                        обучении, остальные обозначены как "Другие педагоги".<br/>Нажав на иконку
                        профиля, можно перейти в профиль преподавателя.
                        {getImg(kizo11, 'Страница "Педагоги"')}
                    </div>
                </>}
                {zag[type].role < 2 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Завучи
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Нажав на иконку профиля, можно перейти в профиль завуча.
                        {getImg(kizo12, 'Страница "Завучи"')}
                    </div>
                </>}
                {zag[type].role == 0 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Одноклассники
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Нажав на иконку профиля, можно перейти в профиль одноклассника.
                        {getImg(kizo13, 'Страница "Одноклассники"')}
                    </div>
                </>}
                {zag[type].role == 0 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Родители
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        На этой страничке представлены одноклассники и их родители.<br/>
                        Нажав на иконку профиля, можно перейти в профиль родителя.
                        {getImg(kizo14, 'Страница "Родители"')}
                    </div>
                </>}
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Администраторы портала
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Нажав на иконку профиля, можно перейти в профиль администратора портала.
                    {getImg(kizo15, 'Страница "Администраторы портала"')}
                </div>
            </div>
        </div>
    )
}

function getGenRec() {
    return (
        <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i+" rek"}>
            <div className={tutorCSS.zag} id={tutorCSS.nav_i}>
                Общие рекомендации
            </div>
            <div className={tutorCSS.block}>
                Рекомендации будут в виде рубрики "Вопрос-ответ", а также определённых тем.<br/>
                Возможно информация будет дополняться по мере появления вопросов у пользователей.
                <br/>(Блоки с вопросами скрываются и раскрываются нажатием на них)
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Как зарегистрироваться?
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Возможность регистрации закрыта для посторонних.
                    {getImg(kizo2, "Регистрация скрыта")}
                    Вам должна придти ссылка-приглашение от учебного центра.
                    {getImg(kizo1, "Пример ссылки")}
                    Перейдите по ссылке. Если ссылка, которую вам отправили верна,
                    то вам откроется возможность зарегистрировать новый аккаунт.
                    {getImg(kizo3, "Регистрация доступна")}
                    Также имеется возможность добавить роль к существующему аккаунту. Для этого
                    необходимо быть авторизованным и перейти по ссылке-приглашению.
                    {getImg(kizo4, "Роль успешно добавлена")}
                    В случае ошибки в приглашении или истечении его срока действия будет
                    показано оповещение...
                    {getImg(kizo5, "Ошибка в оповещении")}
                    Для успешной регистрации необходимо заполнить все поля и
                    принять условия соглашения. Разрешены только латиница и
                    цифры.
                    {getImg(kizo6, "Регистрация")}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Восстановление пароля
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Чтобы была возможность восстановления пароля, необходимо установить
                    секретную фразу. Для этого нужно перейти в настройки, нажать на
                    "Добавить секретную фразу". Заполнить соответствующее поле и
                    подтвердить.
                    {getImg(kizo8, "Установка секретной фразы")}
                    В случае, если вы прошли данную процедуру, то необходимо перейти к странице
                    авторизации и зайти при помощи текста-ссылки "Забыли пароль?" в интерфейс
                    восстановления пароля.
                    {getImg(kizo2, "Переход к интерфейсу")}
                    Для успешной смены пароля необходимо заполнить все поля. Для всех полей
                    кроме "Секретной фразы" разрешены только латинница и цифры.
                    {getImg(kizo7, "Интерфейс смены пароля")}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Настройка темы
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    По умолчанию тема устанавливается с учётом настроек вашей системы. Но вы
                    всегда можете воспользоваться соответствующим переключателем в левом нижнем
                    углу страницы.
                    {getImg(kizo9, "Настройка темы")}
                </div>
                <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                    Смена роли
                </div>
                <div className={tutorCSS.blockOtv} data-act="0">
                    Если у вас на аккаунте имеется какая-то другая роль, то есть возможность
                    переключаться между ними.<br/>
                    При наведении мыши на ваш логин, раскроется меню с соответствующей кнопкой.
                    {getImg(kizo10, "Смена роли")}
                </div>
                {zag[type].role == 1 && <>
                    <div className={tutorCSS.zag1} id={tutorCSS.nav_i} onClick={visB}>
                        Смена наблюдаемого учащегося
                    </div>
                    <div className={tutorCSS.blockOtv} data-act="0">
                        Если вы имеете несколько детей, то у вас есть возможность наблюдать за
                        всеми, переключаясь между ними.
                        {getImg(pizo1, "Смена наблюдаемого учащегося")}
                    </div>
                </>}
            </div>
        </div>
    )
}

function getImg(src, descr) {
    return (
        <div className={tutorCSS.blockImg}>
            <img src={src} alt=""/>
            {descr}
        </div>
    )
}

function tim() {
    if (scrolling) {
        scrolling = false;
        knop();
    }
}

function getZag(text, link, b) {
    return (
        <div className={tutorCSS.zag1} id={tutorCSS.nav_i} ref={b ? (el)=>endSod=el : undefined} onClick={() => goTo(link)}>
            {text}
        </div>
    )
}

function onCon(e) {
    send({
        type: "TUTOR",
        uuid: cState.uuid
    }, 'POST', "auth", "infCon");
}

function goTo(id) {
    document.querySelector("." + id).scrollIntoView(true);
    let sinc = window.scrollY - Math.round(window.innerHeight / 100) * 7;
    window.scrollTo(0, sinc);
    knop();
}

function knop() {
    let x = endSod.getBoundingClientRect().top + Math.round(window.innerHeight / 100) * 7;
    CWSel.setAttribute("data-act", x > 0 ? "0" : "1");
}

function visB(e) {
    let el = e.target.nextElementSibling;
    el.setAttribute("data-act", el.getAttribute("data-act") == "0" ? "1" : "0");
}

export function Tutor() {
    const themeState = useSelector(themes);
    let { typ } = useParams();
    cState = useSelector(states);
    dispatch = useDispatch();
    if(!type || type != typ) type = typ;
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Tutor.jsx");
        window.onwheel = (e) => {
            if(!scrolling) {
                scrolling = true;
                ev = e;
                timid = setTimeout(tim,1000);
            }
        };
        knop();
        setActived(zag[type].link);
        eventSource.addEventListener('connect', onCon, false);
        return function() {
            dispatch = undefined;
            window.onwheel = undefined;
            clearTimeout(timid);
            eventSource.removeEventListener('connect', onCon);
            console.log("I was triggered during componentWillUnmount Tutor.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Tutor.jsx');
    });
    return (
        <div className={tutorCSS.AppHeader}>
            <Helmet>
                <title>{zag[type].name}</title>
            </Helmet>
            <div className={tutorCSS.blockPro}>
                <div className={tutorCSS.pro}>
                    {(!cState.auth && zag[type].role == 3) && <Request/>}
                    <div className={tutorCSS.nav_iZag+" "+tutorCSS.nav_i}>
                        <div className={tutorCSS.zag+" soder"} id={tutorCSS.nav_i}>
                            Содержание
                        </div>
                        <div className={tutorCSS.block}>
                            Описание: заголовки ниже кликабельны. Нажимая на них можно перейти к интересующему вас
                            разделу. При дальнейшем скролле, когда содержание полностью скроется появится кнопочка
                            возвращающая к содержанию.
                            {(!cState.auth && zag[type].role == 3) && getZag("Заявка на подключение", "req")}
                            {getZag("Общие рекомендации", "rek")}
                            {zag[type].role == 3 && getZag("Объявления", "news")}
                            {zag[type].role == 3 && getZag("Контакты", "cont")}
                            {getZag("Люди", "pep")}
                            {zag[type].role < 2 && getZag("Дневник", "dnev")}
                            {zag[type].role < 2 && getZag("Аналитика", "ana")}
                            {zag[type].role == 2 && getZag("Журнал", "jur")}
                            {zag[type].role == 3 && getZag("Администрирование УО", "admYO")}
                            {getZag("Профиль", "prof")}
                            {getZag("Настройки", "set", true)}
                        </div>
                    </div>
                    {getGenRec()}
                    {zag[type].role == 3 && getNews()}
                    {zag[type].role == 3 && getContacts()}
                    {getPeople()}
                    {zag[type].role < 2 && getDnevnik()}
                    {zag[type].role < 2 && getAnalytic()}
                    {zag[type].role == 2 && getZhur()}
                    {zag[type].role == 3 && getAdminYO()}
                    {getProfil()}
                    {getSettings()}
                </div>
            </div>
            <div className={tutorCSS.GotCW} ref={(el)=>CWSel = el}>
                <div>
                    <img src={knopka} alt="" onClick={() => goTo("soder")}/>
                    <div className={tutorCSS.GotCWText}>
                        Перейти к содержанию
                    </div>
                </div>
            </div>
        </div>
    )
}
export default Tutor;