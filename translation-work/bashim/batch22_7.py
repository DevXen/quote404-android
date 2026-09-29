#!/usr/bin/env python3
import json, re, sys

BASE = '/home/hatch/workspace/translation-work/bashim'
START, COUNT = 700, 100

en = {}
en[404769] = (
'Shurik: Yesterday we trudged sadly from the stadium, some 2-3 thousand people to Frunzenskaya\n'
'Shurik: Crossed the road in a stream and blocked it off to a motorcade of about seven cars\n'
'Shurik: That was the first time in my life I heard cops say "Please LET US through" into a megaphone )'
)
en[404770] = (
'<COBECTb> hey everyone, anyone here?\n'
'<CableMan> <COBECTb>, all right, all right, I\'m already going to do my coursework! Just don\'t bug me here!!!'
)
en[404771] = (
'XXX: Anyone know how long hamsters live (jungariks)?\n'
'YYY: 2-3 years... but they breed fast - instead of two I had nine after six months!\n'
'XXX: Then another question: anyone know how to cook hamsters?'
)
en[404772] = (
'Navar: you can\'t eat in bed\n'
'Navar: then the sheet\'s full of crumbs - knees hurt'
)
en[404773] = (
'Saladyn\n'
'if everyone from our office disappears from ICQ and there\'s no ping to our IPs, know that I was fixing the heater'
)
en[404774] = (
'teacher: he said: "The longer I live, the..." what?\n'
'student: the less is left to live'
)
en[404775] = (
'DimOn: Clients from Kakhovka complained the server got slower. Couldn\'t figure it out remotely, looked fine. Came out. Server room 3 by 3 meters. Two computers in it. No ventilation. Door tightly locked with a key\n'
'Pil$on: heh\n'
'Pil$on: put one of their employees in there for a day'
)
en[404776] = (
'Rat_S: clocks are going back soon )\n'
'Rat_S: catching my drift?\n'
'Chikishnya: no\n'
'Rat_S: hint - on which day can you play two hours of bowling paying for one?'
)
en[404777] = (
'xxx: I came to hate politics in general and deputies in particular in deep childhood\n'
'xxx: When once, instead of DuckTales they aired a live broadcast of a people\'s deputies assembly'
)
en[404778] = (
'PunkCTpax: So today I found 500 rubles on the street )\n'
'Alexey: Happy birthday! I was afraid someone else would snatch it.)'
)
en[404779] = (
'Sigiz: Spider-Man and other gene-perverted mutants are all crap :) We had it cooler. And much earlier - just recall the world\'s first frogman Vasilisa the Wise from the Russian folk tale :)'
)
en[404780] = (
'Crystal: do you know why this happens?\n'
'Crystal: when I leave - I put the comp to sleep, when I come back - it\'s already awake!\n'
'Crystal: all by itself\n'
'Renix: means it got enough sleep.'
)
en[404781] = (
'<Yokel> a couple more Germans get ejected and our guys will start rolling )\n'
'<GromHS> 9 more\n'
'<GromHS> and we\'ll score\n'
'<Yokel> yeah\n'
'<Yokel> the main thing is they eject the goalie\n'
'<Yokel> or it might not roll'
)
en[404782] = (
'Saw on answers.mail.ru:\n'
'Alexander *** asks:\n'
'\n'
'Day before yesterday a thrush flew into my house, sat on the cornice, I caught it. I had bugs I fed my iguana, and I gave the thrush a dozen bugs, it ate them, then I released it out the window. My iguana saw all this and now she hates and despises me, won\'t sit on my arms, keeps biting me. What do I do, how do I get my pet\'s love for its owner back? Help.\n'
'\n'
'Added 3 minutes ago\n'
'This isn\'t funny, we\'ve lived with her for 2 years, and now this (\n'
'I haven\'t fed her for a day already, she\'s still mad.'
)
en[404783] = (
'Garrinych: If I master Linux, my skin will be covered in a sweater.'
)
en[404784] = (
'Baddaddy: Hi :)\n'
'Mary_Jane: hi!\n'
'Baddaddy: You know what day it is today? :)\n'
'Mary_Jane: no idea, Monday?\n'
'Baddaddy: A year ago I wrote to you in ICQ, and you told me you were busy and would write later. Well, a year has passed :)'
)
en[404785] = (
'Dr Stein: A girl once texted me an SMS. I\'ll never forget it. It was two pages. The first looked like this: "Sorry, I\'ll write you later, my dad died, we\'re eating his"... \n'
'Dr Stein: And the second: "...picking him up from the morgue"'
)
en[404786] = (
'xxx: I\'m the smartest in my group (at uni). At the first class they told everyone: "Forget everything you were taught at school!!!" \n'
'Apparently everyone did forget.\n'
'And I wasn\'t at uni that day...'
)
en[404787] = (
'Novosibirsk, Akademgorodok.\n'
'Two hairy metalheads standing, a typical gopnik walks up to them:\n'
'- Guys, I didn\'t come to mess with you... give me, please, a cigarette!'
)
en[404788] = (
'<xxx> Imagine you\'re going to the USA tomorrow, you\'ll have everything there, what will you take with you?\n'
'<yyy> A dozen keyboards, Russian ones.'
)
en[404789] = (
'An aging lady professor tells:\n'
'- In my time all the guys were into Hemingway. Each had a beard and a sweater, and each wanted to get a six-toed cat...'
)
en[404790] = (
'(12:48) xxx: Dear **** employees, today a DOTA championship will be organized, at 17:00 I\'m waiting for all who wish in room 309.\n'
'(13:23) xxx: Championship canceled! I got fired ((( '
)
en[404791] = (
'xxx: how are you over there, on the other end of the line?\n'
'yyy: no way\n'
'xxx: what do you mean no way?\n'
'yyy: I have wifi'
)
en[404792] = (
'tatarin_art: A colleague at work asks about the weather - Has nobody been outside recently?\n'
'tatarin_art: I in a gloomy sad voice - they went out, but nobody came back'
)
en[404793] = (
'Asterot> In refutation of Newton\'s law, the bed also has its own gravity.\n'
'Asterot> And it\'s inversely proportional to the time on the clock: the less the time, the greater the bed\'s gravity.\n'
'Asterot> In short, f...ck getting up at six in the morning ('
)
en[404794] = (
'KLer@: handwriting input on the phone taught me what school and college tried to achieve with no success - legible handwriting'
)
en[404795] = (
'AlexRWS: If you plug an old electric shaver into a wired-radio socket, it will tell you all kinds of funny things.\n'
'povezlo: We won\'t even mention the iron!\n'
'Demurg: I also heard about a soldering iron. They say if you stick it up your ass, you can hear a live recording of Sepultura\'s last concert.'
)
en[404796] = (
'Were treating the cat. Due to stomach problems, had to feed it oatmeal, which it, naturally, refused to eat! In general the whole process looked more like torture. Then the situation: dad and I are standing in the kitchen, he\'s holding the cat by its front paws and muzzle, I, accordingly, by its back ones with one hand, with the other - shoving oatmeal into its mouth with a spoon, the cat in the "Christ crucifixion" pose, my shoulder is torn up, blood is trickling down my arm, dad is all in porridge, with bitten fingers, in a torn t-shirt, a hysterical cat wail echoes through the whole apartment in a voice hoarse from screaming. Mom walks in, observes the whole scene and proclaims:\n'
'- But the oatmeal, compared to the medicine, he eats with pleasure!'
)
en[404797] = (
'LOCO: calling a student\'s mom\n'
'LOCO: so and so, your little one is being expelled, she has 10 debts, she\'s expelled as of the first\n'
'LOCO: she says to me "Oh my God, you\'re confusing us, our Vickochka studies on a scholarship"\n'
'LOCO: so I\'m thinking, where\'s she getting the scholarship?'
)
en[404798] = (
'Yasherko: Most Popular Themes in Twitter: 1) yay, snow! 2) f*ck, snow!'
)
en[404799] = (
'she:\n'
'suppose you have a girlfriend who wants either a sphinx (a bald cat) or a chihuahua (a fat rat); who would you choose, since you have to live with it?\n'
'\n'
'he:\n'
'another girlfriend.'
)
en[404800] = (
'PbICbKA: I only sell tongue-and-groove cutters for woodworking machines\n'
'Nyquist: aaah, my friend sells woodworking machines\n'
'Nyquist: and also vibrators\n'
'PbICbKA: cool\n'
'Nyquist: what are you happy about, they\'re construction ones\n'
'PbICbKA: I\'m happy for the construction guys ;)'
)
en[404801] = (
'shaDoff: I lost my cat\n'
'shaDoff: came home - she\'s not there\n'
'shaDoff: searched both rooms, the kitchen, all the cabinets she can open\n'
'shaDoff: I call - silence\n'
'shaDoff: I get food out - silence\n'
'shaDoff: I think - well that\'s it, she left when I went out\n'
'shaDoff: and this... in short, bitch - locked herself in the washing machine in the bathroom and is sleeping'
)
en[404802] = (
'it_fila: grandma "packed" me lunch for work...\n'
'it_fila: it\'s almost lunchtime now, and I\'m only finishing breakfast.'
)
en[404803] = (
'Ushastik: this video has a 25th frame!!!\n'
'admin: Watched the storyboard in Premiere. In this shitty video, in the shitty quality of the shitty 3gp format there isn\'t even a shitty 24th frame\n'
'admin: Because it\'s 20 frames per second'
)
en[404804] = (
'xxx: they just dragged a printer in for repair... \n'
'xxx: we took it apart... \n'
'xxx: suggested the client reword the ticket to: "My ashtray stopped printing"'
)
en[404805] = (
'Neshika: Hands off our Annushka!\n'
'santos: And don\'t give her any oil.'
)
en[404806] = (
'Sitting at a lecture, texting my brother:\n'
'me: Igor, are you home?\n'
'brother: Yes, and you?'
)
en[404807] = (
'xxx- You know, this movie makes my cod run cold in my veins.\n'
'xxx- *blood\n'
'yyy- You\'re a real programmer.'
)
en[404808] = (
'Yozhik:\n'
'yesterday on the client\'s site I was doing tweaks, the vacancies page. made a typo they laughed at so hard they couldn\'t explain right away. After sending a resume the message: "Your resume has been sent and will be reviewed shortly". just one letter, but what an effect...'
)
en[404809] = (
'John_Chip: If you find a bug on our site, select it with the mouse and press altf+f4'
)
en[404810] = (
'xxx: Hi! Can you tell me one thing?\n'
'yyy: hi\n'
'yyy: I\'ll try\n'
'xxx: How are you?'
)
en[404811] = (
'xxx: Since childhood I deduced a rule for myself\n'
'xxx: Any unknown button must be pressed an even number of times!'
)
en[404812] = (
'macaron: The most sneaky Antivirus I remember is McAfee\n'
'macaron: Downloaded it just to see what it was. Decided to run a system check. And when that bastard was scanning the drive and found its own uninstall, it recognized it as a virus. And by default deleted it...\n'
'macaron: So I sat with it for half a year until I reinstalled Windows.'
)
en[404813] = (
'sssr1945: You know what a happy IP is?\n'
'sssr1945: If the sum of the first two numbers and the last two numbers is equal - then the IP is happy. Then you have to eat a network cable, and any wish will come true.'
)
en[404814] = (
'Andrey: good night\n'
'DataGreed: lizard\n'
'DataGreed: blech\n'
'Andrey: :D\n'
'DataGreed: sweet dreams )'
)
en[404815] = (
'xxx: which is correct? two tortA or two tOrta?\n'
'yyy: correct is - Torte 2 pcs.'
)
en[404816] = (
'xxx\n'
'yesterday morning an SMS came from an unknown number: "Hi, Roma, I\'m Natalia". waited till lunch, then called anyway, curious who it is. Turned out it\'s a salesgirl from the countryside, she got her hands on a 50-ruble bill with my number and the signature Roma on it. ) in short she decided to joke like that, well I asked her to cross it out so it doesn\'t travel across Russia.\n'
'Now an SMS came from another unknown number - "Hi Roma, I\'m Peter, Natalia sends her regards" - the chick\'s on fire )\n'
'\n'
'yyy\n'
'You can call Peter back!'
)
en[404817] = (
'andrey: Hey Sanya, tell me as a lawyer, if my folks give me an apartment after the wedding, can my wife claim it in a divorce?\n'
'tydysh: If after the wedding, then yes - as jointly acquired, if before the wedding, then no.\n'
'andrey: Ha! Lenka\'s a fool!! She told my parents she won\'t marry me until they give me an apartment. :)'
)
en[404818] = (
'xxx: it\'s a long story, and it should start with me getting hit by diarrhea!\n'
'yyy: come on!\n'
'xxx: oh.. Evgeny Viktorovich, sorry, this isn\'t for you :-\\[\n'
'yyy: no way! you got me intrigued - now tell it.'
)
en[404819] = (
'Johnny: ...and at our corporate party everyone came all dressed up. The men in suits, the women in evening gowns. Even our sysadmin - wore a new sweater and combed his beard. )'
)
en[404820] = (
'xxx:\n'
'Yesterday I witnessed a lady gop-stop\n'
'\n'
'xxx:\n'
'A crowd of girls standing. A girl walks past them\n'
'- Hey, you got lipstick? Let me put some on?\n'
'- And a mirror? I\'m quick...'
)
en[404821] = (
'Alex: you\'re going to be shocked by this\n'
'Alex: don\'t want to traumatize your psyche\n'
'Elli: I listened to Sektor Gaza at 10, send it'
)
en[404822] = (
'xxx: hey, was the clay teapot in the kitchen yours?\n'
'yyy: oops\n'
'yyy: broke it?\n'
'xxx: broke it. =(\n'
'yyy: you know, starting to talk about it with a phrase containing the word "was" is like calling from the hospital and asking: "may I speak with the widow of Ivanov Ivan Ivanovich?.."'
)
en[404823] = (
'I\'m thrilled with our teachers.\n'
'\n'
'My son was given homework at school where, among other things, there was the question "how are the letter \'A\' and a bull connected?"\n'
'\n'
'Told him about the Phoenician alphabet, as the first phonetic writing. That there was a letter "aleph", very similar to our modern "A", and that the word "aleph" meant "bull". That maybe the letter was named that because if you flip it, it looks like a bull\'s face with horns.\n'
'\n'
'Also was very glad they tell kids in second grade such things.\n'
'\n'
'The teacher gave the kid a D, saying he fantasized in his homework. And the correct answer: if you add "a" to the word "bull", you get the genitive case.\n'
'\n'
'I hadn\'t planned at such an early age to tell my son that half the people around are idiots, but apparently I\'ll have to :-)'
)
en[404824] = (
'xxx: where\'s the price list, you lazy ass?\n'
'yyy: You, Fyodor Andreevich, have apparently worked too much and not noticed how your manner of communicating with colleagues acquired a shade of familiarity, and I resolutely refuse to cooperate with boors and ask you to be more restrained henceforth.\n'
'xxx: That phrase, which you, Andrey Alekseevich, found familiar, was caused by the fact that due to the lack of a current price list our department cannot perform the lion\'s share of its direct duties on implementing the company\'s new service, and I personally am under enormous pressure from my direct management, forced to pay for the downtime of qualified personnel. So I\'m by no means the boor you deigned to certify me as, but merely a diligent subordinate carrying out the order of the well-known to you Pavel Olegovich, formulated by him as follows: "Fuck the contractors until they deliver the price list!"\n'
'xxx: Therefore I humbly ask you to send us the finished price list in the near future.\n'
'xxx: You lazy ass!'
)
en[404825] = (
'xxx: our university just turned 50 \n'
'xxx: in honor of this a kids\' drawing contest, teachers\' kids draw how they work\n'
'xxx: so the son of the informatics teacher (the teacher is 27) drew by hand a screenshot from Counter-Strike )'
)
en[404826] = (
'xxx: I think our marketer is a satanist. Just now it turned out that on two ad brochures, a booklet and several print runs of leaflets it says: "Company XXXX - a young, DEMONICALLY developing provider..."'
)
en[404827] = (
'ctype: copying a chunk of text, a really big one (like 20 screens) and then pasting it into the console and this thing starts pasting fast at first and then the speed drops to like 30-40 characters per second, is that fixable somehow?\n'
'Obvious: i just wonder, why are you doing that?\n'
'Obvious: it sounds kind of like "tried to swallow a sausage whole, the first 10cm fine, and the rest slowly"'
)
en[404828] = (
'She: Well, what do you think one can do in a cinema? =)\n'
'She: Besides watching movies\n'
'He: eat\n'
'She: Anything else?)\n'
'He: Drink =)\n'
'She: What if you didn\'t come to the cinema alone? )\n'
'He: You\'re something else, of course not alone! There\'s a whole cinema of people there!'
)
en[404829] = (
'He: do you have total commander?\n'
'She: total commander, that\'s my dad...'
)
en[404830] = (
'xxx: Drilling a hole in the wall takes me from 30 seconds to a minute...\n'
'xxx: SO WHY THE FUCK WHEREVER I LIVE DO MY NEIGHBORS TAKE HALF THE NIGHT TO DO IT?!'
)
en[404831] = (
'Arien: Once a year we go with my husband to Crimea for work. Next time before the business trip I\'ll order myself a t-shirt saying "APARTMENT NOT NEEDED"'
)
en[404832] = (
'- And I have two separate computers - one for work, one for personal life.\n'
'- And how many gigs does the personal life take?'
)
en[404833] = (
'she: I have 10 questions for you. First: can you name in one word what you\'re hiding from me?\n'
'he: no\n'
'she: damn, the other 9 questions are gone =('
)
en[404834] = (
'xxx: they just advertised on TV a crystal ball that normalizes the energy in your apartment!\n'
'yyy: 8 centuries on the household artifact market?'
)
en[404835] = (
'Me: Please type the password in small English letters.\n'
'Client: I don\'t know English, I studied German\n'
'Me: Then in small German ones'
)
en[404837] = (
'Tumbus: Napoleon was defeated in fairly warm weather: temperatures dropped to -5 at most.\n'
'Pointer: Minus five is below Napoleon\'s freezing temperature. Know your physics.'
)
en[404838] = (
'xxx:\n'
'The matchstick thing is crap.\n'
'\n'
'xxx:\n'
'Morning. Dorm. Just totally sleep-deprived. You walk into the elevator (no lights by default to save money) and only notice a Black guy in dark clothes because he\'s looking at you and smiling sweetly. After that you go gray.'
)
en[404839] = (
'Lintar: Twilight\'s fine. Only minus for the father of the main heroine, seeing her off to a romantic date with a vampire, giving her a pepper spray can and not garlic-flavored condoms.'
)
en[404840] = (
'xxx: I don\'t eat sausage, for example. it\'s one thing to buy a piece of meat, another - to look at a dead animal. I, for example, when I went to the village, since childhood always hid somewhere if they were slaughtering a pig, so I wouldn\'t hear. and birds, when they\'re slaughtered - can\'t watch. to this day.\n'
'\n'
'yyy: I think you don\'t use Kaspersky either )'
)
en[404841] = (
'Axis: ok. where do you live territorially?\n'
'RaMiRo[BZ]: Yaroslavl highway\n'
'Axis: omg. you got really spread out )'
)
en[404842] = (
'Seryoga: time flies fast\n'
'Sawyer: you don\'t say\n'
'Sawyer: already starting to feel the ass thickening over my head at uni...'
)
en[404843] = (
'alex_mashin: We live in a unique era, when most social network users are still alive. It won\'t go on like this forever: the day will come when LiveJournal will start looking like a cemetery, and it\'ll need renaming to DeadJournal.'
)
en[404844] = (
'from a torrent\n'
'\n'
'xxx:\n'
'Folks, give me speed, it\'s impossible to download! It says the download will finish in 3 weeks, and I\'m leaving for the army...'
)
en[404845] = (
'SHE: sitting in front of the laptop, face expression "don\'t touch me, I\'m all in there"\n'
'SHE: I say to him: "Seryozh, there\'s a mosquito on the ceiling"\n'
'SHE: with the same "Imperturbable_Buddha_Stare" he looks at the ceiling, finds where the mosquito sits\n'
'SHE: turns back to the laptop, reaches with his left hand to the nightstand,\n'
'SHE: takes the TV REMOTE, points it at the mosquito and presses "turn off" )\n'
'SHE: and all this without looking away from the laptop. With a completely vacant face!\n'
'SHE: either he reached Enlightenment, or I live with a programmer-zombie...'
)
en[404846] = (
'Romka:\n'
'Apartment description: 110 minutes by transport from Vykhino metro station....\n'
'\n'
'Romka:\n'
'people have totally lost it, renting out apartments from neighboring regions in the Moscow database to make them cost more\n'
'\n'
'Romka:\n'
'maybe we should throw Novosibirsk ones in here too? Paveletskaya metro station, 2500 minutes by transport. What\'s your most central street there?'
)
en[404847] = (
'Sineglazka: Just to live, and be needed by somebody - that\'s already happiness!\n'
'Lianote: The military enlistment office needs me, f*ck, I\'m happy!'
)
en[404848] = (
'jcd: just needed to run a couple commands in the windows console\n'
'jcd: opening the console and out of habit first thing I do is ls -la\n'
'jcd: and glancing at the output I see that it WORKED\n'
'jcd: nearly gave me a heart attack.\n'
'jcd: for half a minute I stare at the monitor with a wild dazed look\n'
'jcd: and only then it hits me that half an hour ago I installed MinGW and added its path to the environment variables\n'
'jcd: and happiness was so close\n'
'jcd: :)'
)
en[404849] = (
'<mmm> What do you think, if people who drag out paying the salary for a site for a week find the site not opening tomorrow morning - is that a subtle enough hint? )'
)
en[404850] = (
'Husband: Just imagine. You have a cool car. Beautiful. Your Dream!\n'
'Wife: Well, imagined, and? =)\n'
'Husband: So. This car can\'t drive for a week because it has maintenance, and before that for 2 weeks it\'s preparing for it and can\'t drive either. And the rest of the time it only drives when it feels like it. How do you like that?\n'
'Wife: Why the hell would I need such a car?! =)\n'
'Wife: Wait!... what are you hinting at?!!!'
)
en[404851] = (
'xxx: I < 3 U!\n'
'yyy: Well yeah, iodine is less than three uraniums \n'
'yyy: And then what?'
)
en[404852] = (
'The start of the third night. I have the radio on. A sleepy DJ, apparently fully convinced nobody hears him:\n'
'- Well, you\'ve listened to the new track of the king of pop Michael Jackson "This Is It". As you can see, death wasn\'t an obstacle to the musician\'s career. Let\'s hope for more hits from Michael and wish him creative success...'
)
en[404853] = (
'Lena: Continue the phrase \'You are very . . . . .\'\n'
'Myself: untimely'
)
en[404854] = (
'Lana: So these jerks caught a May beetle, took my nail polish and made a ladybug out of it!'
)
en[404855] = (
'xxx: Doesn\'t it seem to you that Allods was ripped off of WoW?\n'
'yyy: in general all games are ripped off of life\n'
'zzz: And the orc race was ripped off of my neighbors =)'
)
en[404856] = (
'Mik: Damn, how long since I\'ve seen you. We haven\'t talked since childhood. So many years passed! So what\'s new?\n'
'Anutkin: I trimmed my bangs!! What do you think, will they suit me?'
)
en[404857] = (
'She: like somebody always loved to nitpick, but now this somebody leveled up their skill to "old grumpy fart"\n'
'She: sorry, kitty, I love you, but sometimes you\'re unbearable\n'
'He: I know.\n'
'He: You forgot the soft sign in the word "nitpick".'
)
en[404858] = (
'xxx: Oh yeah, the ideal guy. How I\'d like it to be a hero, like in books - tall, stately, strong. With long dark or light, hair. With some incredible eyes of an amazing color - amber, violet, red...\n'
'yyy: A linuxoid! Hair - long, eyes - red, says incomprehensible words and commands demons. Like in anime right now!'
)
en[404859] = (
'yyy: by the way, thanks to your grandma ;-)\n'
'xxx: for what?\n'
'yyy: thanks to her we developed a new strategy for handling grandmas\' complaints. Now we call them to solve problems when a series is on. the conversation no longer lasts more than five minutes and they agree with everything'
)
en[404860] = (
'xxx: found it! my wife left me alone with the one-year-old kid, ordering me not to put diapers on him - let the skin breathe. And the kid, while I was staring at the comp, climbed into the closet, took out my wife\'s jewelry, including gold, scattered it on the floor and shat on it! that\'s how you handle all this mercantile crap!\n'
'yyy: raised a socialist...'
)
en[404861] = (
'<woody> once I make my car - I\'ll post pics\n'
'<woody> I want to trim its inside a bit with wood. should turn out cozy!\n'
'<MIC> yeah, and sand it with a plane )\n'
'<MIC> I can lay tiles, if you need coziness in the car, just call me.'
)
en[404862] = (
'xxx: I\'m saying, when I opened the document, something like a little window opened on the side, and it said what to do with this document\n'
'yyy: And?\n'
'xxx: well, and now it\'s not there, and it looks kind of uncomfortable, I looked in the settings, nothing adjusts\n'
'yyy: which office do you have?\n'
'xxx: what do you mean which? a 3-room one...'
)
en[404863] = (
'xxx: I have an acquaintance who offered a client on site to install a shoe cleaning system. To which he thankfully replied: you mean a doormat?'
)
en[404864] = (
'xxx: someone put toilet bowl cleaner on the sink instead of liquid soap...\n'
'yyy: I think they\'re trying to tell you where your hands grow from'
)
en[404865] = (
'#Delsian: Reminded me of the popular several years ago answer to the question "Why expand channels if the existing ones are enough":\n'
'You\'ve seen a toilet\'s outgoing pipe? Well it\'s that thick not because traffic is big, but to pass the existing traffic peaks at maximum speed.'
)
en[404866] = (
'xxx: Yura, at what stage is the ad layout development?\n'
'yyy: At the "oh f*ck, right, the ad layout!" stage'
)
en[404867] = (
'xxx: \n'
'Decided with dad to run in the mornings. Went to the store, bought sneakers, tracksuits ,)\n'
'\n'
'xxx: \n'
'First morning, 6 am, park near the house. Running at an average pace, a bum is sitting on the lawn, clearly hungover, follows us with a hazy look and a phrase drifts after us "Men....where to...the kiosk\'s still closed..."'
)
en[404868] = (
'xxx\n'
'Did Dima get better?\n'
'yyy\n'
'Not yet\n'
'xxx\n'
'bye'
)
en[404869] = (
'Noodle: I\'m learning to cook bit by bit.\n'
'Noodle: I burn pans, ruin pots.'
)

expected_ids = [404769 + n for n in range(101) if n != 67]  # 404836 missing in source
def main():
    src_lines = open(BASE + '/chunks/chunk_22.jsonl', encoding='utf-8').read().splitlines()
    recs = [json.loads(l) for l in src_lines[START:START+COUNT]]
    got = [r['id'] for r in recs]
    assert got == expected_ids, 'ID order mismatch: %s' % (got[:5],)
    assert set(en.keys()) == set(expected_ids), 'missing/extra ids: %s' % sorted(set(expected_ids) ^ set(en.keys()))
    bad_nl = [(r['id'], r['text'].count('\n'), en[r['id']].count('\n')) for r in recs if r['text'].count('\n') != en[r['id']].count('\n')]
    if bad_nl:
        print('NEWLINE MISMATCH: %s' % (bad_nl,))
        sys.exit(1)
    bad_tok = []
    for r in recs:
        for t in set(re.findall(r'<[^>\n]*>', r['text'])):
            if t not in en[r['id']]:
                bad_tok.append((r['id'], t))
    if bad_tok:
        print('TOKEN MISMATCH: %s' % (bad_tok,))
        sys.exit(1)
    out = open(BASE + '/out/chunk_22.jsonl', 'a', encoding='utf-8')
    for r in recs:
        out.write(json.dumps({'id': r['id'], 'ru': r['text'], 'en': en[r['id']]}, ensure_ascii=False) + '\n')
    out.close()
    n = sum(1 for _ in open(BASE + '/out/chunk_22.jsonl', encoding='utf-8'))
    print('appended %d, out now has %d lines' % (COUNT, n))

main()
